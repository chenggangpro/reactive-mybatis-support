/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.transaction.parallel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionDefinition;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.simple.SimpleQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.transaction.update.UpdateMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author evans
 * @version 1.0.0
 * @since 2.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
class ParallelTransactionTest extends MybatisR2dbcApplicationTests {

    int parallelCount = 100;

    @Autowired
    UpdateMapper updateMapper;
    @Autowired
    SimpleQueryMapper simpleQueryMapper;

    @Test
    void parallelMultipleTransaction() {
        // this test doesn't work with r2dbc-mssql driver
        // r2dbc-mssql 0.9.0 has an issue fixed in 1.0.2.RELEASE but the r2dbc-spi's baseline is 1.0.0.RELEASE
        // issue link: https://github.com/r2dbc/r2dbc-mssql/issues/271
        if (!MySQLContainer.class.equals(currentContainerType) && !MariaDBContainer.class.equals(currentContainerType)) {
            return;
        }
        ExecutorService executorService = Executors.newScheduledThreadPool(16);
        Tuple3<AtomicInteger, AtomicInteger, AtomicInteger> results = Tuples.of(new AtomicInteger(0),
                new AtomicInteger(0),
                new AtomicInteger(0)
        );
        Flux.range(0, parallelCount)
                .flatMap(loop -> {
                    if (loop % 2 == 0) {
                        return Mono.fromCompletionStage(CompletableFuture
                                .runAsync(() -> {
                                    super.transactionalOperator(defaultTransactionDefinition -> {
                                                defaultTransactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                                                defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                                            }).execute(status -> {
                                                status.setRollbackOnly();
                                                Dept dept = new Dept();
                                                dept.setDeptNo(1L);
                                                dept.setDeptName("INSET_DEPT_NAME1");
                                                dept.setLocation("INSET_DEPT_LOCATION");
                                                dept.setCreateTime(LocalDateTime.now());
                                                return updateMapper.updateDeptByDeptNo(dept)
                                                        .then(simpleQueryMapper.selectByDeptNo(1L));
                                            })
                                            .as(StepVerifier::create)
                                            .consumeNextWith(dept -> {
                                                Assertions.assertEquals(dept.getDeptName(), "INSET_DEPT_NAME1");
                                            })
                                            .verifyComplete();
                                }, executorService));
                    } else {
                        return Mono.fromCompletionStage(CompletableFuture.runAsync(
                                () -> {
                                    super.transactionalOperator(defaultTransactionDefinition -> {
                                                defaultTransactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                                                defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                                            }).execute(status -> {
                                                status.setRollbackOnly();
                                                Dept dept = new Dept();
                                                dept.setDeptNo(1L);
                                                dept.setDeptName("INSET_DEPT_NAME2");
                                                dept.setLocation("INSET_DEPT_LOCATION");
                                                dept.setCreateTime(LocalDateTime.now());
                                                return simpleQueryMapper.selectByDeptNo(1L)
                                                        .doOnNext(oldDept -> {
                                                            boolean readUnCommitted = "INSET_DEPT_NAME1".equals(oldDept.getDeptName());
                                                            boolean readCurrent = "INSET_DEPT_NAME2".equals(oldDept.getDeptName());
                                                            boolean readOriginal = !readUnCommitted && !readCurrent;
                                                            if (readUnCommitted) {
                                                                results.getT1().getAndIncrement();
                                                            }
                                                            if (readCurrent) {
                                                                results.getT2().getAndIncrement();
                                                            }
                                                            if (readOriginal) {
                                                                results.getT3().getAndIncrement();
                                                            }
                                                        })
                                                        .flatMap(oldDept -> {
                                                            return updateMapper.updateDeptByDeptNo(dept)
                                                                    .then(simpleQueryMapper.selectByDeptNo(1L))
                                                                    .doOnNext(newDept -> Assertions.assertEquals(newDept.getDeptName(),
                                                                            "INSET_DEPT_NAME2"
                                                                    ));
                                                        });
                                            })
                                            .as(StepVerifier::create)
                                            .expectNextCount(1)
                                            .verifyComplete();
                                }, executorService
                        ));
                    }
                }, 16)
                .subscribe(__ -> {});
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //ignore
        } finally {
            executorService.shutdownNow();
        }
        System.out.println("-------- Parallel Result: ");
        System.out.println("Read uncommitted: " + results.getT1().get());
        System.out.println("Read current    : " + results.getT2().get());
        System.out.println("Read original   : " + results.getT3().get());
        System.out.println("--------------------------");
    }
}

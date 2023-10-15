/*
 *    Copyright 2009-2023 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend.DeptWithEmp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gang Cheng
 */
public class BusinessServiceTests extends TestApplicationTests {

    @Autowired
    private BusinessService businessService;

    @Test
    public void testDoWithoutTransaction() throws Exception {
        int count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                businessService.doWithoutTransaction()
                        .as(StepVerifier::create)
                        .expectNextCount(1)
                        .verifyComplete();
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
    }

    @Test
    public void testDoWithTransactionBusiness() {
        businessService.doWithTransactionBusiness()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testDoWithTransactionBusinessRollback() throws Exception {
        businessService.doWithTransactionBusinessRollback()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> {
                    return "manually rollback with @Transaction".equals(throwable.getMessage());
                })
                .verify();
    }

    @Test
    public void testDoWithoutTransactionThenWithTransaction() {
        businessService.doWithoutTransaction()
                .flatMap(deptWithEmp -> {
                    assertThat(deptWithEmp)
                            .extracting(DeptWithEmp::getEmpList)
                            .matches(empList -> empList.size() > 0);
                    return businessService.doWithTransactionBusinessRollback();
                })
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> {
                    return "manually rollback with @Transaction".equals(throwable.getMessage());
                })
                .verify();
    }
}

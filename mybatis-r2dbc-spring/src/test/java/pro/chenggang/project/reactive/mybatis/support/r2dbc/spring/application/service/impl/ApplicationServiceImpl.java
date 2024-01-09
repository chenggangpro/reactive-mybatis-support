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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.simple.SimpleQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.transaction.delete.DeleteMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.transaction.insert.InsertMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.ApplicationService;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final SimpleQueryMapper simpleQueryMapper;
    private final InsertMapper insertMapper;
    private final DeleteMapper deleteMapper;

    @Override
    public Mono<Void> runWithoutTransaction() {
        return simpleQueryMapper.countAllDept()
                .flatMap(count -> {
                    Dept dept = new Dept();
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return insertMapper.insertOneDeptWithGeneratedKey(dept)
                            .doOnNext(effectRowCount -> {
                                log.info("Insert one dept effectRowCount : {}", effectRowCount);
                                assert effectRowCount > 0;
                            })
                            .then(simpleQueryMapper.countAllDept())
                            .doOnNext(countAfterInsertion -> {
                                log.info("Before insertion : {}, After insertion : {}", count, countAfterInsertion);
                                assert countAfterInsertion == count + 1;
                            })
                            .then(Mono.defer(() -> deleteMapper.deleteByDeptNo(dept.getDeptNo())))
                            .then();
                });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<Void> runWithTransactionCommit() {
        return simpleQueryMapper.countAllDept()
                .flatMap(count -> {
                    Dept dept = new Dept();
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return insertMapper.insertOneDeptWithGeneratedKey(dept)
                            .doOnNext(effectRowCount -> {
                                log.info("Insert one dept effectRowCount : {}", effectRowCount);
                                assert effectRowCount > 0;
                            })
                            .then(simpleQueryMapper.countAllDept())
                            .doOnNext(countAfterInsertion -> {
                                log.info("Before insertion : {}, After insertion : {}", count, countAfterInsertion);
                                assert countAfterInsertion == count + 1;
                            })
                            .then(Mono.defer(() -> deleteMapper.deleteByDeptNo(dept.getDeptNo())))
                            .doOnNext(effectRowCount -> {
                                log.info("Delete one dept effectRowCount : {}", effectRowCount);
                                assert effectRowCount > 0;
                            })
                            .then(simpleQueryMapper.countAllDept())
                            .doOnNext(countAfterDeletion -> {
                                log.info("Before insertion : {}, After deletion : {}", count, countAfterDeletion);
                                assert Objects.equals(countAfterDeletion, count);
                            });
                })
                .then();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<Void> runWithTransactionRollback() {
        return simpleQueryMapper.countAllDept()
                .flatMap(count -> {
                    Dept dept = new Dept();
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return insertMapper.insertOneDeptWithGeneratedKey(dept)
                            .doOnNext(effectRowCount -> {
                                log.info("Insert one dept(1) effectRowCount : {}", effectRowCount);
                                assert effectRowCount > 0;
                            })
                            .then(simpleQueryMapper.countAllDept())
                            .doOnNext(countAfterInsertion -> {
                                log.info("Before insertion : {}, After insertion : {}", count, countAfterInsertion);
                                assert countAfterInsertion == count + 1;
                            })
                            .then(Mono.defer(() -> {
                                dept.setDeptNo(null);
                                return insertMapper.insertOneDeptWithGeneratedKey(dept);
                            }))
                            .doOnNext(effectRowCount -> {
                                log.info("Insert one dept(2) effectRowCount : {}", effectRowCount);
                                assert effectRowCount > 0;
                            })
                            .then(Mono.defer(
                                    () -> Mono.error(new IllegalStateException("Mock exception for transaction rollback")))
                            );
                })
                .then();
    }
}

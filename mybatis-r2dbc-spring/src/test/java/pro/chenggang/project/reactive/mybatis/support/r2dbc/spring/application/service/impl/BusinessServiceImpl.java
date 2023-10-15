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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend.DeptWithEmp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.deptNo;

/**
 * @author Gang Cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final DeptMapper deptMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<Dept> doWithTransactionBusiness() {
        return this.doBusinessInternal();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<Dept> doWithTransactionBusinessRollback() {
        return this.doBusinessInternal()
                .then(Mono.defer(() -> {
                    if (true) {
                        throw new RuntimeException("manually rollback with @Transaction");
                    }
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<DeptWithEmp> doWithoutTransaction() {
        return deptMapper.count()
                .filter(count -> count > 0)
                .flatMap(count -> deptMapper.selectDeptWithEmpList()
                        .take(1, true)
                        .singleOrEmpty()
                );

    }

    private Mono<Dept> doBusinessInternal() {
        return deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L)))
                .doOnNext(people -> log.debug("[Before] Get People ,People:{}", people))
                .flatMap(people -> deptMapper.updateSelective(new Dept()
                                .setDeptName("InsertDept")
                                .setLocation("InsertLocation")
                                .setCreateTime(LocalDateTime.now()),
                        dsl -> dsl.and(deptNo, isEqualTo(4L))))
                .flatMap(value -> deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L))))
                .doOnNext(updatePeople -> log.debug("[After Update] Get People ,People:{}", updatePeople))
                .flatMap(updatePeople -> deptMapper.delete(dsl -> dsl.where(deptNo, isEqualTo(4L))))
                .flatMap(deleteResult -> deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L))));
    }
}

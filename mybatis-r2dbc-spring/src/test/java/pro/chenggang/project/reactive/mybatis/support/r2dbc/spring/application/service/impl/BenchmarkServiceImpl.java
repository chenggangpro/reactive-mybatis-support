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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BenchmarkService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Gang Cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenchmarkServiceImpl implements BenchmarkService {

    private final DeptMapper deptMapper;

    @Override
    public Mono<?> getFromDb() {
        return deptMapper.selectDeptWithEmpList()
                .collectList();
    }

    @Override
    public Mono<?> getFromDb(String id) {
        return deptMapper.selectOneByDeptNo(Long.parseLong(id))
                .cast(Object.class)
                .switchIfEmpty(Mono.defer(() -> Mono.just("data not exist")));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<?> insertDb() {
        Dept dept = new Dept();
        dept.setDeptName(UUID.randomUUID().toString());
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation(UUID.randomUUID().toString());
        return deptMapper.insertSelective(dept)
                .flatMap(insertResult -> Mono.just("Generated DeptNo: " + dept.getDeptNo()));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Mono<?> insertThenDeleteDb() {
        Dept dept = new Dept();
        dept.setDeptName(UUID.randomUUID().toString());
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation(UUID.randomUUID().toString());
        return deptMapper.insertSelective(dept)
                .flatMap(insertResult -> deptMapper.deleteByDeptNo(dept.getDeptNo())
                        .flatMap(deleteResult -> deptMapper.count()
                                .map(countResult -> "TotalCount : " + countResult))
                );
    }
}

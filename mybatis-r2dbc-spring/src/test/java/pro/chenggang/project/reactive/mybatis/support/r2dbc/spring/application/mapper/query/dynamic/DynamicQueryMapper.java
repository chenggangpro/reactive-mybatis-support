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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.dynamic;

import org.apache.ibatis.annotations.Mapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonCountMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonSelectMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.mapper.dynamic.DeptDynamicMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.mapper.dynamic.DeptDynamicSqlSupport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface DynamicQueryMapper extends CommonCountMapper, CommonSelectMapper, DeptDynamicMapper {

    default Mono<Long> countAllDept() {
        return count(dsl -> dsl);
    }

    default Mono<Dept> selectByDeptNo(Long deptNo) {
        return selectOne(dsl -> dsl
                .where(DeptDynamicSqlSupport.deptNo, isEqualTo(deptNo))
        );
    }

    default Flux<Dept> selectAllDept() {
        return select(dsl -> dsl);
    }

}

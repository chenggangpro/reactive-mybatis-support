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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.delete;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonDeleteMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import reactor.core.publisher.Mono;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.dept;

/**
 * The interface Delete mapper.
 *
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface DeleteMapper extends CommonDeleteMapper {

    Mono<Integer> deleteByDeptNo(Long deptNo);

    @Delete("DELETE FROM dept WHERE dept_no = #{deptNo}")
    Mono<Integer> deleteByDeptNoWithAnnotation(Long deptNo);

    default Mono<Integer> deleteByDeptNoWithDynamic(Long deptNo) {
        return ReactiveMyBatis3Utils.deleteFrom(this::delete,
                dept,
                dsl -> dsl.where(DeptDynamicSqlSupport.deptNo, isEqualTo(deptNo))
        );
    }
}

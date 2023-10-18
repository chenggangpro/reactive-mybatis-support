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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.simple;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import reactor.core.publisher.Mono;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface SimpleQueryMapper {

    Mono<Long> countAll();

    Mono<Dept> selectOne();

    Mono<Dept> selectByDeptNo(@Param("deptNo") Long deptNo);

    @Results(id = "selectByDeptNoWithAnnotationResult", value = {
            @Result(column = "dept_no", property = "deptNo", jdbcType = JdbcType.BIGINT, id = true),
            @Result(column = "dept_name", property = "deptName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "location", property = "location", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("SELECT * FROM dept WHERE dept_no = #{deptNo}")
    Mono<Dept> selectByDeptNoWithAnnotatedResult(@Param("deptNo") Long deptNo);

    Mono<Dept> selectByDeptNoWithResultMap(@Param("deptNo") Long deptNo);
}

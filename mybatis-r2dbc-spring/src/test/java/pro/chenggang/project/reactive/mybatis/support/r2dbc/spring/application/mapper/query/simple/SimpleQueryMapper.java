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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.simple;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.extend.DeptWithEmpList;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.extend.EmpWithDept;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface SimpleQueryMapper {

    Mono<Long> countAllDept();

    Mono<Dept> selectOneDept();

    Mono<Dept> selectOneDeptMssql();

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

    Mono<Dept> selectByDeptNoWithConstructorResultMap(@Param("deptNo") Long deptNo);

    @ConstructorArgs({
            @Arg(column = "dept_no", name = "deptNo", javaType = Long.class, id = true),
            @Arg(column = "dept_name", name = "deptName", javaType = String.class),
            @Arg(column = "location", name = "location", javaType = String.class),
            @Arg(column = "create_time", name = "createTime", javaType = LocalDateTime.class)
    })
    @Select("SELECT * FROM dept WHERE dept_no = #{deptNo}")
    Mono<Dept> selectByDeptNoWithAnnotatedConstructor(@Param("deptNo") Long deptNo);
    
    Mono<DeptWithEmpList> selectDeptWithEmpList(@Param("deptNo") Long deptNo);

    Mono<EmpWithDept> selectEmpWithDept(@Param("empNo") Long empNo);
}

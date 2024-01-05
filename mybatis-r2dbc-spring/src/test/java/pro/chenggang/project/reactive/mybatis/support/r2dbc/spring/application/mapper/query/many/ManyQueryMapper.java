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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.many;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Emp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.extend.DeptWithEmpList;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.extend.EmpWithDept;
import reactor.core.publisher.Flux;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ManyQueryMapper {

    Flux<Dept> selectAllDept(@Param("deptName") String deptName);

    Flux<DeptWithEmpList> selectAllDeptWithEmpList();

    Flux<DeptWithEmpList> selectAllDeptWithEmpListOrdered();

    Flux<Emp> selectAllEmpWithOrdered(@Param("empName") String empName);

    Flux<EmpWithDept> selectAllEmpWithDept();

    Flux<EmpWithDept> selectAllEmpWithDeptOrdered();

}

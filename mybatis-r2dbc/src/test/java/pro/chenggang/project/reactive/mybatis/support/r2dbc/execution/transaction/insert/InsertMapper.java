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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.insert;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectContent;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonInsertMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.createTime;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.dept;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.deptName;
import static pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic.DeptDynamicSqlSupport.location;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface InsertMapper extends CommonInsertMapper<Dept> {

    Mono<Integer> insertOneDept(Dept dept);

    Mono<Integer> insertOneDeptWithGeneratedKey(Dept dept);

    Mono<Integer> insertOneDeptWithGeneratedKeyUsingSelectKey(Dept dept);

    @Insert("INSERT INTO dept (dept_name, location, create_time) VALUES (#{deptName},#{location},#{createTime})")
    Mono<Integer> insertOneDeptWithAnnotation(Dept dept);

    @Insert("INSERT INTO dept (dept_name, location, create_time) VALUES (#{deptName},#{location},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "deptNo", keyColumn = "dept_no")
    Mono<Integer> insertOneDeptWithGeneratedKeyAndAnnotation(Dept dept);

    @SelectKey(databaseId = "postgresql", statement = "SELECT currval('dept_dept_no_seq')", keyColumn = "dept_no", keyProperty = "deptNo", resultType = Long.class, before = false)
    @SelectKey(databaseId = "mysql", statement = "SELECT LAST_INSERT_ID()", keyColumn = "dept_no", keyProperty = "deptNo", resultType = Long.class, before = false)
    @SelectKey(databaseId = "mariadb", statement = "SELECT LAST_INSERT_ID()", keyColumn = "dept_no", keyProperty = "deptNo", resultType = Long.class, before = false)
    @SelectKey(databaseId = "oracle", statement = "SELECT dept_seq.currval FROM DUAL", keyColumn = "dept_no", keyProperty = "deptNo", resultType = Long.class, before = false)
    @Insert("INSERT INTO dept (dept_name, location, create_time) VALUES (#{deptName},#{location},#{createTime})")
    Mono<Integer> insertOneDeptWithGeneratedKeyAndAnnotationAndSelectKey(Dept dept);

    Mono<Integer> insertMultipleDept(List<Dept> deptList);

    @Insert(databaseId = "oracle", value = {
            "<script>",
            "INSERT ALL",
            "<foreach collection='list' item='item' separator=' '>" +
                    "  INTO dept (dept_name, location, create_time) VALUES (#{item.deptName},#{item.location},#{item.createTime})" +
                    "</foreach>" +
                    "SELECT * FROM DUAL",
            "</script>"
    })
    @Insert(databaseId = "mysql",value = {
            "<script>",
            "INSERT INTO dept (dept_name, location, create_time) VALUES",
            "<foreach collection='list' item='item' separator=','>" +
                    "(#{item.deptName},#{item.location},#{item.createTime})" +
                    "</foreach>",
            "</script>"
    })
    @Insert(databaseId = "mariadb",value = {
            "<script>",
            "INSERT INTO dept (dept_name, location, create_time) VALUES",
            "<foreach collection='list' item='item' separator=','>" +
                    "(#{item.deptName},#{item.location},#{item.createTime})" +
                    "</foreach>",
            "</script>"
    })
    @Insert(databaseId = "postgresql",value = {
            "<script>",
            "INSERT INTO dept (dept_name, location, create_time) VALUES",
            "<foreach collection='list' item='item' separator=','>" +
                    "(#{item.deptName},#{item.location},#{item.createTime})" +
                    "</foreach>",
            "</script>"
    })
    @Insert(databaseId = "mssql",value = {
            "<script>",
            "INSERT INTO dept (dept_name, location, create_time) VALUES",
            "<foreach collection='list' item='item' separator=','>" +
                    "(#{item.deptName},#{item.location},#{item.createTime})" +
                    "</foreach>",
            "</script>"
    })
    Mono<Integer> insertMultipleDeptWithAnnotation(List<Dept> deptList);

    @InsertProvider(type = SqlProviderAdapter.class, method = "insert")
    @Options(useGeneratedKeys = true, keyProperty = "row.deptNo", keyColumn = "dept_no")
    Mono<Integer> insertWthGeneratedKey(InsertStatementProvider<Dept> insertStatement);

    default Mono<Integer> insertWithDynamic(Dept row) {
        return ReactiveMyBatis3Utils.insert(this::insert, row, dept, c ->
                c.map(deptName).toProperty("deptName")
                        .map(location).toProperty("location")
                        .map(createTime).toProperty("createTime")
        );
    }

    default Mono<Integer> insertAndGeneratedKeyWithDynamic(Dept row) {
        return ReactiveMyBatis3Utils.insert(this::insertWthGeneratedKey, row, dept, c ->
                c.map(deptName).toProperty("deptName")
                        .map(location).toProperty("location")
                        .map(createTime).toProperty("createTime")
        );
    }

    default Mono<Integer> insertMultipleWithDynamic(Collection<Dept> records) {
        return ReactiveMyBatis3Utils.insertMultiple(this::insertMultiple, records, dept, c ->
                c.map(deptName).toProperty("deptName")
                        .map(location).toProperty("location")
                        .map(createTime).toProperty("createTime")
        );
    }

    Mono<Integer> insertWithBlobAndClod(SubjectContent subjectContent);
}

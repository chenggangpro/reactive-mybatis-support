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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.where.WhereApplier;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonCountMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonDeleteMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonInsertMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonSelectMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.CommonUpdateMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic.ReactiveMyBatis3Utils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.createTime;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.dept;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.deptName;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.deptNo;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.location;

/**
 * auto generated
 * @author AutoGenerated
 */
@Mapper
public interface DeptDynamicMapper extends CommonSelectMapper, CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Dept>, CommonUpdateMapper {
    BasicColumn[] selectList = BasicColumn.columnList(deptNo, deptName, location, createTime);

    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys = true,keyProperty = "row.deptNo",keyColumn = "dept_no")
    Mono<Integer> insert(InsertStatementProvider<Dept> insertStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="DeptResult", value = {
        @Result(column="dept_no", property="deptNo", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="dept_name", property="deptName", jdbcType=JdbcType.VARCHAR),
        @Result(column="location", property="location", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP)
    })
    Flux<Dept> selectMany(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("DeptResult")
    Mono<Dept> selectOne(SelectStatementProvider selectStatement);

    default Mono<Long> count(CountDSLCompleter completer) {
        return ReactiveMyBatis3Utils.countFrom(this::count, dept, completer);
    }

    default Mono<Integer> delete(DeleteDSLCompleter completer) {
        return ReactiveMyBatis3Utils.deleteFrom(this::delete, dept, completer);
    }

    default Mono<Integer> insert(Dept row) {
        return ReactiveMyBatis3Utils.insert(this::insert, row, dept, c ->
            c.map(deptNo).toProperty("deptNo")
            .map(deptName).toProperty("deptName")
            .map(location).toProperty("location")
            .map(createTime).toProperty("createTime")
        );
    }

    default Mono<Integer> insertMultiple(Collection<Dept> records) {
        return ReactiveMyBatis3Utils.insertMultiple(this::insertMultiple, records, dept, c ->
            c.map(deptNo).toProperty("deptNo")
            .map(deptName).toProperty("deptName")
            .map(location).toProperty("location")
            .map(createTime).toProperty("createTime")
        );
    }

    default Mono<Integer> insertSelective(Dept row) {
        return ReactiveMyBatis3Utils.insert(this::insert, row, dept, c ->
            c.map(deptNo).toPropertyWhenPresent("deptNo", row::getDeptNo)
            .map(deptName).toPropertyWhenPresent("deptName", row::getDeptName)
            .map(location).toPropertyWhenPresent("location", row::getLocation)
            .map(createTime).toPropertyWhenPresent("createTime", row::getCreateTime)
        );
    }

    default Mono<Dept> selectOne(SelectDSLCompleter completer) {
        return ReactiveMyBatis3Utils.selectOne(this::selectOne, selectList, dept, completer);
    }

    default Flux<Dept> select(SelectDSLCompleter completer) {
        return ReactiveMyBatis3Utils.selectList(this::selectMany, selectList, dept, completer);
    }

    default Flux<Dept> selectDistinct(SelectDSLCompleter completer) {
        return ReactiveMyBatis3Utils.selectDistinct(this::selectMany, selectList, dept, completer);
    }

    default Mono<Integer> update(UpdateDSLCompleter completer) {
        return ReactiveMyBatis3Utils.update(this::update, dept, completer);
    }

    default Mono<Integer> updateSelectiveByPrimaryKey(Dept row) {
        return update(c ->
            c.set(deptName).equalToWhenPresent(row::getDeptName)
            .set(location).equalToWhenPresent(row::getLocation)
            .set(createTime).equalToWhenPresent(row::getCreateTime)
            .where(deptNo, isEqualTo(row::getDeptNo))
        );
    }

    default Mono<Integer> updateAllByPrimaryKey(Dept row) {
        return update(c ->
            c.set(deptName).equalToWhenPresent(row::getDeptName)
            .set(location).equalToWhenPresent(row::getLocation)
            .set(createTime).equalToWhenPresent(row::getCreateTime)
            .where(deptNo, isEqualTo(row::getDeptNo))
        );
    }

    default Mono<Integer> updateAll(Dept row, WhereApplier whereApplier) {
        return update(c ->
            c.set(deptName).equalToWhenPresent(row::getDeptName)
            .set(location).equalToWhenPresent(row::getLocation)
            .set(createTime).equalToWhenPresent(row::getCreateTime)
            .applyWhere(whereApplier)
        );
    }

    default Mono<Integer> updateSelective(Dept row, WhereApplier whereApplier) {
        return update(c ->
            c.set(deptName).equalToWhenPresent(row::getDeptName)
            .set(location).equalToWhenPresent(row::getLocation)
            .set(createTime).equalToWhenPresent(row::getCreateTime)
            .applyWhere(whereApplier)
        );
    }
}
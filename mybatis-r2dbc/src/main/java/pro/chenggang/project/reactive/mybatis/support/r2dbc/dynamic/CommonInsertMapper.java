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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.dynamic;

import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import reactor.core.publisher.Mono;

/**
 * This is a general purpose mapper for executing various types of insert statements. This mapper is appropriate for
 * insert statements that do NOT expect generated keys.
 *
 * @param <T>
 *            the type of record associated with this mapper
 */
public interface CommonInsertMapper<T> extends CommonGeneralInsertMapper {
    /**
     * Execute an insert statement with input fields mapped to values in a POJO.
     *
     * @param insertStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insert")
    Mono<Integer> insert(InsertStatementProvider<T> insertStatement);

    /**
     * Execute an insert statement that inserts multiple rows. The row values are supplied by mapping to values in a
     * List of POJOs.
     *
     * @param insertStatement the insert statement
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "generalInsert")
    Mono<Integer> generalInsert(GeneralInsertStatementProvider insertStatement);

    /**
     * Execute an insert statement with input fields supplied by a select statement.
     *
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insertMultiple")
    Mono<Integer> insertMultiple(MultiRowInsertStatementProvider<T> insertStatement);

}

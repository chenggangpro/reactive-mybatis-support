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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive sql session.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSession {

    /**
     * The constant DEFAULT_PROFILE of ReactiveSqlSessionProfile.
     */
    ReactiveSqlSessionProfile DEFAULT_PROFILE = ReactiveSqlSessionProfile.of(false, null, true);

    /**
     * Retrieve a single row mapped from the statement key.
     *
     * @param <T>       the returned object type
     * @param statement the statement
     * @return Mapped object
     */
    default <T> Mono<T> selectOne(String statement) {
        return selectOne(statement, null);
    }

    /**
     * Retrieve a single row mapped from the statement key and parameter.
     *
     * @param <T>       the returned object type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return Mapped object
     */
    <T> Mono<T> selectOne(String statement, Object parameter);

    /**
     * Retrieve many mapped objects from the statement key.
     *
     * @param <E>       the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @return List of mapped object
     */
    default <E> Flux<E> selectList(String statement) {
        return selectList(statement, null);
    }

    /**
     * Retrieve many mapped objects from the statement key and parameter.
     *
     * @param <E>       the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return List of mapped object
     */
    default <E> Flux<E> selectList(String statement, Object parameter) {
        return selectList(statement, parameter, RowBounds.DEFAULT);
    }

    /**
     * Retrieve many mapped objects from the statement key and parameter,
     * within the specified row bounds.
     *
     * @param <E>       the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @param rowBounds Bounds to limit object retrieval
     * @return List of mapped object
     */
    <E> Flux<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    /**
     * Execute an insert statement.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the insert.
     */
    default Mono<Integer> insert(String statement) {
        return insert(statement, null);
    }

    /**
     * Execute an insert statement with the given parameter object. Any generated
     * autoincrement values or selectKey entries will modify the given parameter
     * object properties. Only the number of rows affected will be returned.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the insert.
     */
    Mono<Integer> insert(String statement, Object parameter);

    /**
     * Execute an update statement. The number of rows affected will be returned.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the update.
     */
    default Mono<Integer> update(String statement) {
        return update(statement, null);
    }

    /**
     * Execute an update statement. The number of rows affected will be returned.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the update.
     */
    Mono<Integer> update(String statement, Object parameter);

    /**
     * Execute a delete statement. The number of rows affected will be returned.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the delete.
     */
    default Mono<Integer> delete(String statement) {
        return delete(statement, null);
    }

    /**
     * Execute a delete statement. The number of rows affected will be returned.
     *
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the delete.
     */
    Mono<Integer> delete(String statement, Object parameter);

    /**
     * Perform commits database connection.
     * Note that database connection will not be committed if no updates/deletes/inserts were called.
     * To force the commit call {@link ReactiveSqlSession#commit(boolean)}
     *
     * @return mono Void
     */
    default Mono<Void> commit() {
        return commit(false);
    }

    /**
     * Perform commits database connection.
     *
     * @param force forces connection commit
     * @return mono
     */
    Mono<Void> commit(boolean force);

    /**
     * Perform rollback database connection .
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * To force the rollback call {@link ReactiveSqlSession#rollback(boolean)}
     *
     * @return mono
     */
    default Mono<Void> rollback() {
        return rollback(false);
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     *
     * @param force forces connection rollback
     * @return mono
     */
    Mono<Void> rollback(boolean force);

    /**
     * close session
     *
     * @return mono
     */
    Mono<Void> close();

    /**
     * Retrieves current configuration.
     *
     * @return R2dbcMybatisConfiguration configuration
     */
    R2dbcMybatisConfiguration getConfiguration();

    /**
     * Retrieves a mapper.
     *
     * @param <T>  the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapper(Class<T> type);

}

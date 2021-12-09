package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor;

import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 12/7/21.
 */
public interface ReactiveSqlSession {

    /**
     * enable transaction
     * @return
     */
    ReactiveSqlSession beginTransaction();

    /**
     * Retrieve a single row mapped from the statement key.
     * @param <T> the returned object type
     * @param statement
     *          the statement
     * @return Mapped object
     */
    default <T> Mono<T> selectOne(String statement) {
        return selectOne(statement,null);
    }

    /**
     * Retrieve a single row mapped from the statement key and parameter.
     * @param <T> the returned object type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return Mapped object
     */
    <T> Mono<T> selectOne(String statement, Object parameter);

    /**
     * Retrieve a list of mapped objects from the statement key.
     * @param <E> the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @return List of mapped object
     */
    default <E> Flux<E> selectList(String statement) {
        return selectList(statement,null);
    }

    /**
     * Retrieve a list of mapped objects from the statement key and parameter.
     * @param <E> the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return List of mapped object
     */
    default <E> Flux<E> selectList(String statement, Object parameter) {
        return selectList(statement,parameter,RowBounds.DEFAULT);
    }

    /**
     * Retrieve a list of mapped objects from the statement key and parameter,
     * within the specified row bounds.
     * @param <E> the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @param rowBounds  Bounds to limit object retrieval
     * @return List of mapped object
     */
    <E> Flux<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    /**
     * Execute an insert statement.
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the insert.
     */
    default Mono<Integer> insert(String statement) {
        return insert(statement,null);
    }

    /**
     * Execute an insert statement with the given parameter object. Any generated
     * autoincrement values or selectKey entries will modify the given parameter
     * object properties. Only the number of rows affected will be returned.
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the insert.
     */
    Mono<Integer> insert(String statement, Object parameter);

    /**
     * Execute an update statement. The number of rows affected will be returned.
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the update.
     */
    default Mono<Integer> update(String statement) {
        return update(statement,null);
    }

    /**
     * Execute an update statement. The number of rows affected will be returned.
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the update.
     */
    Mono<Integer> update(String statement, Object parameter);

    /**
     * Execute a delete statement. The number of rows affected will be returned.
     * @param statement Unique identifier matching the statement to execute.
     * @return int The number of rows affected by the delete.
     */
    default Mono<Integer> delete(String statement) {
        return delete(statement,null);
    }

    /**
     * Execute a delete statement. The number of rows affected will be returned.
     * @param statement Unique identifier matching the statement to execute.
     * @param parameter A parameter object to pass to the statement.
     * @return int The number of rows affected by the delete.
     */
    Mono<Integer> delete(String statement, Object parameter);

    /**
     * Flushes batch statements and commits database connection.
     * Note that database connection will not be committed if no updates/deletes/inserts were called.
     * To force the commit call {@link ReactiveSqlSession#commit(boolean)}
     * @return
     */
    default Mono<Void> commit() {
        return commit(false);
    }

    /**
     * Flushes batch statements and commits database connection.
     * @param force forces connection commit
     * @return
     */
    Mono<Void> commit(boolean force);

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * To force the rollback call {@link ReactiveSqlSession#rollback(boolean)}
     * @return
     */
    default Mono<Void> rollback() {
        return rollback(false);
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * @param force forces connection rollback
     * @return
     */
    Mono<Void> rollback(boolean force);

    /**
     * close session
     * @return
     */
    Mono<Void> close();

    /**
     * Retrieves current configuration.
     * @return Configuration
     */
    R2dbcConfiguration getConfiguration();

    /**
     * Retrieves a mapper.
     * @param <T> the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapper(Class<T> type);

}

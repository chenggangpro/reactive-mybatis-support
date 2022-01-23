package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive mybatis executor.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface ReactiveMybatisExecutor {

    /**
     * execute update
     *
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @return mono
     */
    Mono<Integer> update(MappedStatement mappedStatement, Object parameter);

    /**
     * execute query
     *
     * @param <E>             the type parameter
     * @param mappedStatement the mapped statement
     * @param parameter       the parameter
     * @param rowBounds       the row bounds
     * @return flux
     */
    <E> Flux<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds);

    /**
     * commit transaction
     *
     * @param required the required
     * @return mono
     */
    Mono<Void> commit(boolean required);

    /**
     * rollback transaction
     *
     * @param required the required
     * @return mono
     */
    Mono<Void> rollback(boolean required);

    /**
     * close session
     *
     * @param forceRollback the force rollback
     * @return mono
     */
    Mono<Void> close(boolean forceRollback);

}

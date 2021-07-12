package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.IsolationLevel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author: chenggang
 * @date 7/11/21.
 */
public interface ReactiveSqlSessionExecutor {

    /**
     * with isolation level default do nothing
     * @param isolationLevel
     * @return
     */
    default ReactiveSqlSessionExecutor withIsolationLevel(IsolationLevel isolationLevel){
        return this;
    }

    /**
     * Execute a callback {@link Function} within a {@link Connection} scope. The function is responsible for creating a
     * {@link Mono}. The connection is released after the {@link Mono} terminates (or the subscription is cancelled).
     * Connection resources must not be passed outside of the {@link Function} closure, otherwise resources may get
     * defunct.
     *
     * @param connectionFactory  must nut be  {@literal null}.
     * @param action must not be {@literal null}.
     * @return the resulting {@link Mono}.
     */
    <T> Mono<T> inConnection(ConnectionFactory connectionFactory,Function<Connection, Mono<T>> action);

    /**
     * Execute a callback {@link Function} within a {@link Connection} scope. The function is responsible for creating a
     * {@link Flux}. The connection is released after the {@link Flux} terminates (or the subscription is cancelled).
     * Connection resources must not be passed outside of the {@link Function} closure, otherwise resources may get
     * defunct.
     *
     * @param connectionFactory  must nut be  {@literal null}.
     * @param action must not be {@literal null}.
     * @return the resulting {@link Flux}.
     */
    <T> Flux<T> inConnectionMany(ConnectionFactory connectionFactory,Function<Connection, Flux<T>> action);

}

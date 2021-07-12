package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.defaults;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveSqlSessionExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ConnectionCloseHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author: chenggang
 * @date 7/11/21.
 */
public class DefaultReactiveSqlSessionExecutor implements ReactiveSqlSessionExecutor {

    private static final Log log = LogFactory.getLog(DefaultReactiveSqlSessionExecutor.class);

    @Override
    public <T> Mono<T> inConnection(ConnectionFactory connectionFactory, Function<Connection, Mono<T>> action) {
        Mono<ConnectionCloseHolder> connectionMono = Mono.just(connectionFactory)
                .flatMap(factory -> Mono.from(factory.create()))
                .doOnNext(connection -> log.debug("Execute Statement With Mono,Get Connection [" + connection + "] From Connection Factory "))
                .map(connection -> new ConnectionCloseHolder(connectionFactory,connection,this::closeConnection));
        return Mono.usingWhen(connectionMono,
                it -> action.apply(it.getTarget()),
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    @Override
    public <T> Flux<T> inConnectionMany(ConnectionFactory connectionFactory,Function<Connection, Flux<T>> action) {
        Mono<ConnectionCloseHolder> connectionMono = Mono.just(connectionFactory)
                .flatMap(factory -> Mono.from(factory.create()))
                .doOnNext(connection -> log.debug("Execute Statement With Flux,Get Connection [" + connection + "] From Connection Factory "))
                .map(connection -> new ConnectionCloseHolder(connectionFactory,connection,this::closeConnection));
        return Flux.usingWhen(connectionMono,
                it -> action.apply(it.getTarget()),
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    /**
     * Release the {@link Connection}.
     *
     * @param connection to close.
     * @return a {@link Publisher} that completes successfully when the connection is closed.
     */
    protected Mono<Void> closeConnection(ConnectionFactory connectionFactory,Connection connection) {
        return Mono.from(connection.close())
                .onErrorResume(Exception.class,e -> Mono.from(connection.close()));
    }
}

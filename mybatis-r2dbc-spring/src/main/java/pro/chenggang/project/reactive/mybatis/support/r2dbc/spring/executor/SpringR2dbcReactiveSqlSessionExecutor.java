package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.connectionfactory.ConnectionFactoryUtils;
import org.springframework.data.r2dbc.connectionfactory.ConnectionProxy;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveSqlSessionExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.CloseSuppressingInvocationHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ConnectionCloseHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.function.Function;

/**
 * @author: chenggang
 * @date 7/11/21.
 */
public class SpringR2dbcReactiveSqlSessionExecutor implements ReactiveSqlSessionExecutor {

    private static final Log log = LogFactory.getLog(SpringR2dbcReactiveSqlSessionExecutor.class);

    @Override
    public <T> Mono<T> inConnection(ConnectionFactory connectionFactory, Function<Connection, Mono<T>> action) {
        Mono<ConnectionCloseHolder> connectionMono = Mono.from(connectionFactory.create())
                .doOnNext(connection -> log.debug("Execute Statement With Mono,Get Connection ["+connection+"] From Connection Factory "))
                .map(it -> new ConnectionCloseHolder(connectionFactory,it, this::closeConnection));
        return Mono.usingWhen(connectionMono,
                it -> {
                    // Create close-suppressing Connection proxy
                    Connection connectionToUse = this.createConnectionProxy(it.getTarget());
                    return action.apply(connectionToUse);
                },
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    @Override
    public <T> Flux<T> inConnectionMany(ConnectionFactory connectionFactory, Function<Connection, Flux<T>> action) {
        Mono<ConnectionCloseHolder> connectionMono = Mono.from(connectionFactory.create())
                .doOnNext(connection -> log.debug("Execute Statement With Flux,Get Connection ["+connection+"] From Connection Factory "))
                .map(it -> new ConnectionCloseHolder(connectionFactory,it, this::closeConnection));
        return Flux.usingWhen(connectionMono,
                it -> {
                    // Create close-suppressing Connection proxy
                    Connection connectionToUse = this.createConnectionProxy(it.getTarget());
                    return action.apply(connectionToUse);
                },
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    /**
     * Create a close-suppressing proxy for the given R2DBC Connection. Called by the {@code execute} method.
     *
     * @param con the R2DBC Connection to create a proxy for
     * @return the Connection proxy
     */
    protected Connection createConnectionProxy(Connection con) {
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
                new Class<?>[] { ConnectionProxy.class },
                new CloseSuppressingInvocationHandler(con));
    }

    /**
     * Release the {@link Connection}.
     *
     * @param connection to close.
     * @return a {@link Publisher} that completes successfully when the connection is closed.
     */
    protected Publisher<Void> closeConnection(ConnectionFactory connectionFactory,Connection connection) {
        return ConnectionFactoryUtils.currentConnectionFactory(connectionFactory)
                .then()
                .onErrorResume(Exception.class, e -> Mono.from(connection.close()));
    }
}

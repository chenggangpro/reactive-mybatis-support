package pro.chenggang.project.reactive.mybatis.support.r2dbc.support;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * Holder for a connection that makes sure the close action is invoked atomically only once.
 */
public class ConnectionCloseHolder {

    private static final Log log = LogFactory.getLog(ConnectionCloseHolder.class);

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final BiFunction<ConnectionFactory,Connection, Publisher<Void>> closeFunction;

    public ConnectionCloseHolder(ConnectionFactory connectionFactory, Connection connection, BiFunction<ConnectionFactory, Connection, Publisher<Void>> closeFunction) {
        this.connectionFactory = connectionFactory;
        this.connection = connection;
        this.closeFunction = closeFunction;
    }

    public Connection getTarget(){
        return this.connection;
    }

    public Mono<Void> close() {
        return Mono.defer(() -> {
            if (this.isClosed.compareAndSet(false, true)) {
                return Mono.from(this.closeFunction.apply(this.connectionFactory,this.connection))
                        .doOnNext(aVoid -> log.debug("Release Connection ["+connection+"] "));
            }
            return Mono.empty();
        });
    }
}
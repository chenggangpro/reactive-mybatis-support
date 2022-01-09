package pro.chenggang.project.reactive.mybatis.support.r2dbc.connection;

import io.r2dbc.spi.Connection;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Holder for a connection that makes sure the close action is invoked atomically only once.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ConnectionCloseHolder {

    private static final Log log = LogFactory.getLog(ConnectionCloseHolder.class);

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Connection connection;
    private final Function<Connection, Publisher<Void>> closeFunction;

    /**
     * Instantiates a new Connection close holder.
     *
     * @param connection    the connection
     * @param closeFunction the close function
     */
    public ConnectionCloseHolder(Connection connection, Function<Connection, Publisher<Void>> closeFunction) {
        this.connection = connection;
        this.closeFunction = closeFunction;
    }

    /**
     * get target
     *
     * @return connection
     */
    public Connection getTarget() {
        return this.connection;
    }

    /**
     * close
     *
     * @return mono
     */
    public Mono<Void> close() {
        return Mono.defer(() -> {
            if (this.isClosed.compareAndSet(false, true)) {
                return Mono.from(this.closeFunction.apply(this.connection));
            }
            return Mono.empty();
        });
    }

    /**
     * Is closed boolean.
     *
     * @return the boolean
     */
    public boolean isClosed() {
        return this.isClosed.get();
    }
}
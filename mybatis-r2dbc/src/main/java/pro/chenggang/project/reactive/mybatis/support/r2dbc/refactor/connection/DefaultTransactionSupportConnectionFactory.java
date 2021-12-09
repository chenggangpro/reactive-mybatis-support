package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.connection;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.Wrapped;
import reactor.core.publisher.Mono;

/**
 * @author evans
 */
public class DefaultTransactionSupportConnectionFactory implements ConnectionFactory, Wrapped<ConnectionFactory> {

	private final ConnectionFactory targetConnectionFactory;

	/**
	 * Create a new DelegatingConnectionFactory.
	 * @param targetConnectionFactory the target ConnectionFactory
	 */
	public DefaultTransactionSupportConnectionFactory(ConnectionFactory targetConnectionFactory) {
		this.targetConnectionFactory = targetConnectionFactory;
	}

	/**
	 * Return the target ConnectionFactory that this ConnectionFactory delegates to.
	 */
	public ConnectionFactory getTargetConnectionFactory() {
		return this.targetConnectionFactory;
	}

	@Override
	public Mono<? extends Connection> create() {
		return Mono.from(this.targetConnectionFactory.create());
	}

	@Override
	public ConnectionFactoryMetadata getMetadata() {
		return this.targetConnectionFactory.getMetadata();
	}

	@Override
	public ConnectionFactory unwrap() {
		return this.targetConnectionFactory;
	}

}

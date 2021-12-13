package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.connection;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.Wrapped;
import org.springframework.lang.Nullable;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.util.ReflectionUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.support.ProxyInstanceFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author evans
 */
public class DefaultTransactionSupportConnectionFactory implements ConnectionFactory, Wrapped<ConnectionFactory> {

	private final ConnectionFactory targetConnectionFactory;

	public DefaultTransactionSupportConnectionFactory(ConnectionFactory targetConnectionFactory) {
		this.targetConnectionFactory = targetConnectionFactory;
	}

	@Override
	public Mono<? extends Connection> create() {
		return this.getOptionalTransactionAwareConnectionProxy(this.targetConnectionFactory);
	}

	@Override
	public ConnectionFactoryMetadata getMetadata() {
		return this.targetConnectionFactory.getMetadata();
	}

	@Override
	public ConnectionFactory unwrap() {
		return this.targetConnectionFactory;
	}

	/**
	 * close connection factory
	 */
	public void close(){
		if(this.targetConnectionFactory instanceof ConnectionPool){
			ConnectionPool connectionPool = ((ConnectionPool) this.targetConnectionFactory);
			if (!connectionPool.isDisposed()) {
				connectionPool.dispose();
			}
		}
	}

	/**
	 * get optional transaction aware connection based on ReactiveExecutorContext's isUsingTransaction()
	 * @param targetConnectionFactory
	 * @return
	 */
	private Mono<Connection> getOptionalTransactionAwareConnectionProxy(ConnectionFactory targetConnectionFactory) {
		return Mono.deferContextual(contextView -> Mono
				.justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
				.cast(ReactiveExecutorContext.class)
				.flatMap(reactiveExecutorContext -> {
					System.out.println(reactiveExecutorContext.getConnection());
					return Mono.just(reactiveExecutorContext.isUsingTransaction())
							.filter(usingTransaction -> usingTransaction)
							.flatMap(usingTransaction -> Mono.justOrEmpty(reactiveExecutorContext.getConnection())
									.switchIfEmpty(Mono.from(targetConnectionFactory.create())
											.map(newConnection -> {
												//TODO check connection in conetxt
												System.out.println(reactiveExecutorContext.getConnection());
												return this.getConnectionProxy(newConnection, true);
											})
									)
									.doOnNext(newConnection -> {
										reactiveExecutorContext.registerConnection(newConnection);
										System.out.println(reactiveExecutorContext.getConnection());
									})
									//if using transaction then force set auto commit to false
									.flatMap(newConnection -> {
										return Mono.justOrEmpty(reactiveExecutorContext.getIsolationLevel())
												.flatMap(isolationLevel -> Mono.from(newConnection.setTransactionIsolationLevel(isolationLevel))
														.then(Mono.from(newConnection.setAutoCommit(false)))
												)
												.switchIfEmpty(Mono.from(newConnection.setAutoCommit(false)))
												.then(Mono.just(newConnection));
									})
							)
							.switchIfEmpty(Mono.defer(() -> Mono.from(targetConnectionFactory.create())
									.map(newConnection -> {
										return this.getConnectionProxy(newConnection, false);
									}))
									//if not using transaction then set auto commit to original auto commit
									.flatMap(newConnection -> {
										return Mono.from(newConnection.setAutoCommit(reactiveExecutorContext.isAutoCommit()))
												.then(Mono.just(newConnection));
									})
							);
				})
		);
	}

	private Connection getConnectionProxy(Connection connection,boolean suspendClose){
		return ProxyInstanceFactory.newInstanceOfInterfaces(
				Connection.class,
				() -> new TransactionAwareConnection(connection,suspendClose),
				Wrapped.class
		);
	}


	/**
	 * Invocation handler that delegates close calls on R2DBC Connections to
	 * {@link ConnectionFactoryUtils} for being aware of context-bound transactions.
	 */
	private static class TransactionAwareConnection implements InvocationHandler {

		private final Connection connection;

		private boolean closed = false;

		private final boolean suspendClose;

		TransactionAwareConnection(Connection connection, boolean suspendClose) {
			this.connection = connection;
			this.suspendClose = suspendClose;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isToStringMethod(method)) {
				return proxyToString(proxy);
			}
			if (ReflectionUtils.isEqualsMethod(method)) {
				return (proxy == args[0]);
			}
			if (ReflectionUtils.isHashCodeMethod(method)) {
				return System.identityHashCode(proxy);
			}
			switch (method.getName()) {
				case "toString":
					return proxyToString(proxy);
				case "equals":
					return (proxy == args[0]);
				case "hashCode":
					return System.identityHashCode(proxy);
				case "unwrap":
					return this.connection;
				case "close":
					return Mono.deferContextual(contextView -> Mono
							.justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
							.cast(ReactiveExecutorContext.class)
							.flatMap(reactiveExecutorContext -> {
								if(!this.suspendClose){
									return Mono.just(reactiveExecutorContext.isRequireClosed())
											.filter(requireClose -> requireClose)
											.flatMap(requireClose -> {
														return Mono.from(this.connection.close())
																.doOnSubscribe(v -> this.closed = true);
													}
											)
											.onErrorResume(Exception.class,e -> Mono
													.from(this.connection.close())
													.doOnSubscribe(v -> this.closed = true)
											);
								}
								if(reactiveExecutorContext.isForceRollback()){
									return Mono.from(this.connection.rollbackTransaction())
											.onErrorResume(Exception.class,e -> Mono
													.from(this.connection.close())
													.doOnSubscribe(v -> this.closed = true)
											);
								}
								if(reactiveExecutorContext.isForceCommit()){
									return Mono.from(this.connection.commitTransaction())
											.onErrorResume(Exception.class,e -> Mono
													.from(this.connection.close())
													.doOnSubscribe(v -> this.closed = true)
											);
								}
								if(reactiveExecutorContext.isRequireClosed()){
									return Mono
											.just(reactiveExecutorContext.clearConnection())
											.doOnSubscribe(v -> this.closed = true)
											.flatMap(oldConnection -> Mono.empty())
											.onErrorResume(Exception.class,e -> Mono
													.from(this.connection.close())
													.doOnSubscribe(v -> this.closed = true)
											);
								}
								return Mono.empty();
							}));
				case "isClosed":
					return this.closed;
			}

			if (this.closed) {
				throw new IllegalStateException("Connection handle already closed");
			}

			// Invoke method on target Connection.
			try {
				return method.invoke(this.connection, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		private String proxyToString(@Nullable Object proxy) {
			// Allow for differentiating between the proxy and the raw Connection.
			return "Transaction-aware proxy for target Connection [" + this.connection.toString() + "],Original Proxy ["+proxy+"]";
		}

	}
}

/*
 *    Copyright 2009-2025 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.connection;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.IsolationLevel;
import io.r2dbc.spi.Wrapped;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;

/**
 * Default implementation of a transaction-aware connection factory that provides
 * transaction support for R2DBC connections in a reactive MyBatis environment.
 * This factory wraps an underlying connection factory and creates connection proxies
 * that are aware of transaction boundaries and can be managed within the reactive context.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public class DefaultTransactionSupportConnectionFactory implements ConnectionFactory, Wrapped<ConnectionFactory>, Closeable {

    private static final Log log = LogFactory.getLog(DefaultTransactionSupportConnectionFactory.class);

    private final ConnectionFactory targetConnectionFactory;

    /**
     * Instantiates a new Default transaction support connection factory.
     *
     * @param targetConnectionFactory the target connection factory
     */
    public DefaultTransactionSupportConnectionFactory(ConnectionFactory targetConnectionFactory) {
        this.targetConnectionFactory = targetConnectionFactory;
    }

    /**
     * Creates a new R2DBC connection wrapped with transaction support capabilities.
     * This method returns either an existing connection from the current reactive context
     * or creates a new connection proxy configured with transaction settings.
     *
     * @return a Mono that emits a transaction-aware Connection when subscribed
     */
    @Override
    public Mono<? extends Connection> create() {
        return this.getOrCreateConfiguredConnectionProxy();
    }

    /**
     * Retrieves the metadata of the underlying connection factory.
     *
     * @return the ConnectionFactoryMetadata of the wrapped connection factory
     */
    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return this.targetConnectionFactory.getMetadata();
    }

    /**
     * Unwraps and returns the underlying connection factory.
     *
     * @return the original ConnectionFactory that this instance wraps
     */
    @Override
    public ConnectionFactory unwrap() {
        return this.targetConnectionFactory;
    }

    /**
     * Closes the connection factory and disposes of any resources.
     * If the underlying connection factory implements Disposable or Closeable,
     * it will be properly disposed of or closed.
     *
     * @throws IOException if an I/O error occurs during closing
     */
    @Override
    public void close() throws IOException {
        if (this.targetConnectionFactory instanceof Disposable) {
            Disposable disposable = ((Disposable) this.targetConnectionFactory);
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
            return;
        }
        if (this.targetConnectionFactory instanceof Closeable) {
            ((Closeable) this.targetConnectionFactory).close();
        }
    }

    /**
     * Gets an existing connection from the current reactive context or creates a new
     * configured connection proxy. The connection is configured with transaction settings
     * based on the current session profile.
     *
     * @return a Mono that emits a configured Connection proxy
     */
    private Mono<Connection> getOrCreateConfiguredConnectionProxy() {
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> {
                            return reactiveExecutorContext.bindConnection(() -> {
                                        return Mono.from(this.targetConnectionFactory.create())
                                                .map(connection -> this.createConnectionProxy(connection, reactiveExecutorContext.isInTransaction()));
                                    })
                                    .flatMap(isNewConnection -> {
                                        if (isNewConnection) {
                                            return Mono.justOrEmpty(reactiveExecutorContext.getCurrentConnection())
                                                    .flatMap(newConnection -> {
                                                        ReactiveSqlSessionProfile currentSessionProfile = reactiveExecutorContext.getCurrentSessionProfile();
                                                        return this.configureIsolationLevel(newConnection, currentSessionProfile)
                                                                .then(this.configureAutoCommit(newConnection, currentSessionProfile))
                                                                .then(this.configureStatementTimeout(newConnection, currentSessionProfile))
                                                                .then(this.configureLockTimeout(newConnection, currentSessionProfile))
                                                                .then(this.startTransactionIfNecessary(newConnection, currentSessionProfile, reactiveExecutorContext))
                                                                .thenReturn(newConnection);
                                                    });
                                        }
                                        return Mono.justOrEmpty(reactiveExecutorContext.getCurrentConnection());
                                    })
                                    .switchIfEmpty(Mono.error(new IllegalStateException("No connection in reactive context")));
                        }
                );
    }

    /**
     * Configures the transaction isolation level for the given connection based on
     * the session profile settings.
     *
     * @param connection            the R2DBC connection to configure
     * @param currentSessionProfile the session profile containing transaction settings
     * @return a Mono that completes when the isolation level is configured, or empty if not applicable
     */
    private Mono<Void> configureIsolationLevel(Connection connection, ReactiveSqlSessionProfile currentSessionProfile) {
        if (!currentSessionProfile.isEnableTransaction()) {
            return Mono.empty();
        }
        IsolationLevel isolationLevel = currentSessionProfile.getIsolationLevel();
        if (Objects.nonNull(isolationLevel)) {
            log.debug("[Get connection] Configure transaction isolation level with: " + isolationLevel);
            return Flux.from(connection.setTransactionIsolationLevel(isolationLevel))
                    .then();
        }
        return Mono.empty();
    }

    /**
     * Configures the auto-commit behavior for the given connection based on
     * the session profile settings.
     *
     * @param connection            the R2DBC connection to configure
     * @param currentSessionProfile the session profile containing transaction settings
     * @return a Mono that completes when auto-commit is configured, or empty if not applicable
     */
    private Mono<Void> configureAutoCommit(Connection connection, ReactiveSqlSessionProfile currentSessionProfile) {
        boolean autoCommit = currentSessionProfile.isAutoCommit();
        if (!currentSessionProfile.isEnableTransaction() || autoCommit) {
            log.debug("[Get connection] Configure transaction auto commit with true");
            return Flux.from(connection.setAutoCommit(true))
                    .then();
        }
        log.debug("[Get connection] Configure transaction auto commit with false");
        return Flux.from(connection.setAutoCommit(false))
                .then();
    }

    /**
     * Configures the statement timeout for the given connection based on
     * the session profile settings.
     *
     * @param connection            the R2DBC connection to configure
     * @param currentSessionProfile the session profile containing transaction settings
     * @return a Mono that completes when statement timeout is configured, or empty if not applicable
     */
    private Mono<Void> configureStatementTimeout(Connection connection, ReactiveSqlSessionProfile currentSessionProfile) {
        Duration statementTimeout = currentSessionProfile.getStatementTimeout();
        if (Objects.nonNull(statementTimeout)) {
            log.debug("[Get connection] Configure transaction statement timeout with: " + statementTimeout);
            return Flux.from(connection.setStatementTimeout(statementTimeout))
                    .then();
        }
        return Mono.empty();
    }

    /**
     * Configures the lock timeout for the given connection based on
     * the session profile settings.
     *
     * @param connection            the R2DBC connection to configure
     * @param currentSessionProfile the session profile containing transaction settings
     * @return a Mono that completes when lock timeout is configured, or empty if not applicable
     */
    private Mono<Void> configureLockTimeout(Connection connection, ReactiveSqlSessionProfile currentSessionProfile) {
        Duration lockTimeout = currentSessionProfile.getLockTimeout();
        if (Objects.nonNull(lockTimeout)) {
            log.debug("[Get connection] Configure transaction lock timeout with: " + lockTimeout);
            return Flux.from(connection.setLockWaitTimeout(lockTimeout))
                    .then();
        }
        return Mono.empty();
    }

    /**
     * Starts a new transaction on the given connection if necessary based on the session profile
     * and executor context state. A transaction is only started if transactions are enabled in
     * the session profile and the executor context is marked as dirty (indicating pending operations).
     *
     * @param connection              the R2DBC connection on which to start the transaction
     * @param currentSessionProfile   the session profile containing transaction configuration settings
     * @param reactiveExecutorContext the reactive executor context tracking the current execution state
     * @return a Mono that completes when the transaction is started, or empty if no transaction is needed
     */
    private Mono<Void> startTransactionIfNecessary(Connection connection, ReactiveSqlSessionProfile currentSessionProfile, ReactiveExecutorContext reactiveExecutorContext) {
        if (!currentSessionProfile.isEnableTransaction() || !reactiveExecutorContext.isDirty()) {
            return Mono.empty();
        }
        log.debug("[Get connection] Start a new transaction");
        return Flux.from(connection.beginTransaction())
                .then();
    }

    /**
     * Creates a transaction-aware connection proxy that wraps the given connection.
     * The proxy intercepts connection operations to provide transaction management.
     *
     * @param connection   the R2DBC connection to wrap
     * @param suspendClose whether to suspend close operations on this connection
     * @return a Connection proxy with transaction awareness
     */
    private Connection createConnectionProxy(Connection connection, boolean suspendClose) {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                Connection.class,
                () -> new TransactionAwareConnection(connection, suspendClose),
                Wrapped.class
        );
    }


    /**
     * Invocation handler that delegates close calls on R2dbc Connections to
     */
    private static class TransactionAwareConnection implements InvocationHandler {

        private final Connection connection;
        private final boolean suspendClose;
        private boolean closed = false;

        /**
         * Instantiates a new Transaction aware connection.
         *
         * @param connection   the connection
         * @param suspendClose suspend close
         */
        TransactionAwareConnection(Connection connection, boolean suspendClose) {
            this.connection = connection;
            this.suspendClose = suspendClose;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
                    if (this.closed) {
                        return Mono.empty();
                    }
                    return MybatisReactiveContextManager.currentContext()
                            .flatMap(reactiveExecutorContext -> {
                                //process rollback
                                if (reactiveExecutorContext.isRequireToRollback()) {
                                    return this.handleRollback(reactiveExecutorContext);
                                }
                                //process commit
                                if (reactiveExecutorContext.isRequireToCommit()) {
                                    return this.handleCommit(reactiveExecutorContext);
                                }
                                //process close connection
                                if (reactiveExecutorContext.isRequireToClose()) {
                                    return this.executeCloseConnection(reactiveExecutorContext);
                                }
                                //if not suspend close connection then process close connection
                                if (!suspendClose) {
                                    return this.executeCloseConnection(reactiveExecutorContext);
                                }
                                //otherwise, nothing to do ,wait for close connection after all transaction
                                log.trace("[Close connection] neither rollback or commit, nothing to do");
                                return Mono.empty();
                            });
                case "isClosed":
                    return this.closed;
            }

            if (this.closed) {
                throw new IllegalStateException("Connection handle already closed");
            }

            // Invoke method on target Connection.
            try {
                return method.invoke(this.connection, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

        /**
         * Handles transaction rollback for the connection within the given reactive context.
         * If the context requires connection closure, the connection will be closed after rollback.
         *
         * @param reactiveExecutorContext the reactive executor context managing the transaction
         * @return a Mono that completes when rollback is finished
         */
        private Mono<Void> handleRollback(ReactiveExecutorContext reactiveExecutorContext) {
            if (!reactiveExecutorContext.isInTransaction()) {
                return Mono.empty();
            }
            if (reactiveExecutorContext.isRequireToClose()) {
                return this.executeCloseConnection(reactiveExecutorContext);
            }
            log.debug("[Close connection] Rollback transaction");
            return Mono.from(this.connection.rollbackTransaction())
                    .then(Mono.<Void>fromRunnable(reactiveExecutorContext::resetRequireToRollback))
                    .onErrorResume(Exception.class, this::onErrorOperation);
        }

        /**
         * Handles transaction commit for the connection within the given reactive context.
         * If the context requires connection closure, the connection will be closed after commit.
         *
         * @param reactiveExecutorContext the reactive executor context managing the transaction
         * @return a Mono that completes when commit is finished
         */
        private Mono<Void> handleCommit(ReactiveExecutorContext reactiveExecutorContext) {
            if (!reactiveExecutorContext.isInTransaction()) {
                return Mono.empty();
            }
            if (reactiveExecutorContext.isRequireToClose()) {
                return this.executeCloseConnection(reactiveExecutorContext);
            }
            log.debug("[Close connection] Commit connection");
            return Mono.from(this.connection.commitTransaction())
                    .then(Mono.<Void>fromRunnable(reactiveExecutorContext::resetRequireToCommit))
                    .onErrorResume(Exception.class, this::onErrorOperation);
        }

        /**
         * Executes the actual connection closure and clears the connection from the reactive context.
         * This method marks the connection as closed and removes it from the context.
         *
         * @param reactiveExecutorContext the reactive executor context to clear the connection from
         * @return a Mono that completes when the connection is closed and cleared from context
         */
        private Mono<Void> executeCloseConnection(ReactiveExecutorContext reactiveExecutorContext) {
            return Mono.defer(() -> {
                        if (reactiveExecutorContext.isRequireToCommit()) {
                            log.debug("[Close connection] Require to commit transaction before close connection ");
                            return Mono.from(this.connection.commitTransaction())
                                    .then(Mono.fromRunnable(reactiveExecutorContext::resetRequireToCommit));
                        }
                        if (reactiveExecutorContext.isRequireToRollback()) {
                            log.debug("[Close connection] Require to rollback transaction before close connection");
                            return Mono.from(this.connection.rollbackTransaction())
                                    .then(Mono.fromRunnable(reactiveExecutorContext::resetRequireToRollback));
                        }
                        if (reactiveExecutorContext.isInTransaction()) {
                            log.debug("[Close connection] Within transaction to commit transaction before close connection");
                            return Mono.from(this.connection.commitTransaction())
                                    .then(Mono.fromRunnable(reactiveExecutorContext::resetRequireToCommit));
                        }
                        return Mono.<Void>empty();
                    })
                    .doOnSubscribe(s -> this.closed = true)
                    .then(Mono.defer(() -> {
                        log.debug("[Close connection] Connection : " + this.connection);
                        return Mono.from(this.connection.close());
                    }))
                    .then(Mono.<Void>fromRunnable(reactiveExecutorContext::reset))
                    .onErrorResume(Exception.class, this::onErrorOperation);
        }

        /**
         * Handles errors that occur during connection operations by ensuring the connection
         * is properly closed and then propagating the original error.
         *
         * @param e the exception that occurred during the operation
         * @return a Mono that emits the original error after ensuring connection cleanup
         */
        private Mono<Void> onErrorOperation(Exception e) {
            return Mono.from(this.connection.close())
                    .doOnSubscribe(v -> this.closed = true)
                    .then(Mono.error(e));
        }

        /**
         * Generates a string representation of the connection proxy for debugging purposes.
         *
         * @param proxy the proxy instance to generate string representation for
         * @return a descriptive string representation of the transaction-aware connection proxy
         */
        private String proxyToString(Object proxy) {
            return "Transaction-support proxy for target Connection [" + this.connection.toString() + "]";
        }

    }
}

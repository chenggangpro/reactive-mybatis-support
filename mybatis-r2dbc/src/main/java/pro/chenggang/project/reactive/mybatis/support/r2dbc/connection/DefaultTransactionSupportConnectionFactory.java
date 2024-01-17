/*
 *    Copyright 2009-2024 the original author or authors.
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
import io.r2dbc.spi.Wrapped;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The type Default transaction support connection factory. without spring
 *
 * @author Gang Cheng
 * @version 1.0.0
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
    @Override
    public void close() throws IOException{
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
     * get optional transaction aware connection based on ReactiveExecutorContext's isUsingTransaction()
     *
     * @param targetConnectionFactory original ConnectionFactory
     * @return
     */
    private Mono<Connection> getOptionalTransactionAwareConnectionProxy(ConnectionFactory targetConnectionFactory) {
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.justOrEmpty(reactiveExecutorContext.getConnection())
                        .switchIfEmpty(Mono.from(targetConnectionFactory.create())
                                .map(newConnection -> {
                                    log.debug("[Get connection]Old connection not exist ,Create connection : " + newConnection);
                                    return this.createConnectionProxy(newConnection, reactiveExecutorContext.isWithTransaction());
                                })
                        )
                        .doOnNext(transactionConnection -> {
                            log.debug("[Get connection]Bind to context : " + transactionConnection);
                            reactiveExecutorContext.bindConnection(transactionConnection);
                        })
                        //if using transaction then force set auto commit to false
                        .flatMap(newConnection -> {
                            if (reactiveExecutorContext.setActiveTransaction()) {
                                return Mono.justOrEmpty(reactiveExecutorContext.getIsolationLevel())
                                        .flatMap(isolationLevel -> {
                                            log.debug("[Get connection]Transaction isolation level exist : " + isolationLevel);
                                            return Mono.from(newConnection.setTransactionIsolationLevel(isolationLevel));
                                        })
                                        .then(Mono.from(newConnection.setAutoCommit(reactiveExecutorContext.isAutoCommit())))
                                        .then(Mono.from(newConnection.beginTransaction()))
                                        .thenReturn(newConnection);
                            }
                            return Mono.just(newConnection);
                        })
                );
    }

    /**
     * create connection proxy
     *
     * @param connection
     * @param suspendClose
     * @return
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
                                if (reactiveExecutorContext.isForceRollback()) {
                                    return this.handleRollback(reactiveExecutorContext);
                                }
                                //process commit
                                if (reactiveExecutorContext.isForceCommit()) {
                                    return this.handleCommit(reactiveExecutorContext);
                                }
                                //process close connection
                                if (reactiveExecutorContext.isRequireClosed()) {
                                    log.debug("[Close connection]close connection");
                                    return this.executeCloseConnection(reactiveExecutorContext);
                                }
                                //if not suspend close connection then process close connection
                                if (!suspendClose) {
                                    return this.executeCloseConnection(reactiveExecutorContext);
                                }
                                //otherwise, nothing to do ,wait for close connection after all transaction
                                log.trace("[Close connection]neither rollback or commit,nothing to do");
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
         * handle rollback
         *
         * @param reactiveExecutorContext ReactiveExecutorContext
         * @return void
         */
        private Mono<Void> handleRollback(ReactiveExecutorContext reactiveExecutorContext) {
            return Mono.just(reactiveExecutorContext.isRequireClosed())
                    .filter(requireClose -> requireClose)
                    .flatMap(requireClose -> {
                        log.debug("[Close connection]rollback and close connection");
                        return Mono.from(this.connection.rollbackTransaction())
                                .then(Mono.defer(
                                        () -> {
                                            reactiveExecutorContext.setForceRollback(false);
                                            return this.executeCloseConnection(reactiveExecutorContext);
                                        }
                                ));
                    })
                    .switchIfEmpty(Mono.defer(
                            () -> {
                                log.debug("[Close connection]just rollback,not close connection");
                                reactiveExecutorContext.setForceRollback(false);
                                return Mono.from(this.connection.rollbackTransaction())
                                        .onErrorResume(Exception.class, this::onErrorOperation);
                            }
                    ));
        }

        /**
         * handle commit
         *
         * @param reactiveExecutorContext ReactiveExecutorContext
         * @return void
         */
        private Mono<Void> handleCommit(ReactiveExecutorContext reactiveExecutorContext) {
            return Mono.just(reactiveExecutorContext.isRequireClosed())
                    .filter(requireClose -> requireClose)
                    .flatMap(requireClose -> {
                        log.debug("[Close connection]commit and close connection");
                        return Mono.from(this.connection.commitTransaction())
                                .then(Mono.defer(
                                        () -> {
                                            reactiveExecutorContext.setForceCommit(false);
                                            return this.executeCloseConnection(reactiveExecutorContext);
                                        }
                                ));
                    })
                    .switchIfEmpty(Mono.defer(
                            () -> {
                                log.debug("[Close connection]just commit,not close connection");
                                reactiveExecutorContext.setForceCommit(false);
                                return Mono.from(this.connection.commitTransaction())
                                        .onErrorResume(Exception.class, this::onErrorOperation);
                            }
                    ));
        }

        /**
         * execute close connection
         *
         * @param reactiveExecutorContext ReactiveExecutorContext
         * @return
         */
        private Mono<Void> executeCloseConnection(ReactiveExecutorContext reactiveExecutorContext) {
            log.debug("[Close Connection]Connection : " + this.connection);
            return Mono.from(this.connection.close())
                    .doOnSubscribe(s -> this.closed = true)
                    .then(Mono.defer(
                            () -> Mono.justOrEmpty(reactiveExecutorContext.clearConnection())
                                    .flatMap(oldConnection -> {
                                        log.debug("[Close Connection]Clear connection in context : " + oldConnection);
                                        return Mono.empty();
                                    })
                    ))
                    .then()
                    .onErrorResume(Exception.class, this::onErrorOperation);

        }

        /**
         * on error operation
         *
         * @param e exception
         * @return
         */
        private Mono<Void> onErrorOperation(Exception e) {
            return Mono.from(this.connection.close())
                    .doOnSubscribe(v -> this.closed = true)
                    .then(Mono.error(e));
        }

        /**
         * proxy to String
         *
         * @param proxy
         * @return
         */
        private String proxyToString(Object proxy) {
            return "Transaction-support proxy for target Connection [" + this.connection.toString() + "]";
        }

    }
}

/*
 *    Copyright 2009-2023 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Default reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 1.0.10
 * @since 1.0.0
 */
public class DefaultReactiveSqlSessionOperator implements ReactiveSqlSessionOperator {

    private final ReactiveSqlSession reactiveSqlSession;
    private final MybatisReactiveContextManager mybatisReactiveContextManager;

    /**
     * Instantiates a new Default reactive sql session operator.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     */
    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory) {
        this(reactiveSqlSessionFactory, true);
    }

    /**
     * Instantiates a new Default reactive sql session operator.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     * @param enableTransaction         the enable transaction
     */
    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory, boolean enableTransaction) {
        this.reactiveSqlSession = reactiveSqlSessionFactory.openSession().usingTransaction(enableTransaction);
        this.mybatisReactiveContextManager = (MybatisReactiveContextManager) this.reactiveSqlSession;
    }

    @Override
    public <T> Mono<T> execute(Mono<T> monoExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> this.reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Mono<T> executeAndCommit(Mono<T> monoExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Mono<T> executeAndRollback(Mono<T> monoExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeMany(Flux<T> fluxExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> this.reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeManyAndCommit(Flux<T> fluxExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeManyAndRollback(Flux<T> fluxExecution) {
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextManager::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }
}

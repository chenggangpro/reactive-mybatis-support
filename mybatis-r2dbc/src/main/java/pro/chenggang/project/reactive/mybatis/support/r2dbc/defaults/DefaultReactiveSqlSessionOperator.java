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

import java.util.function.Function;

/**
 * The type Default reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 2.0.0
 * @since 1.0.0
 */
public class DefaultReactiveSqlSessionOperator implements ReactiveSqlSessionOperator {

    private final ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    /**
     * Instantiates a new Default reactive sql session operator.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     */
    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory) {
        this.reactiveSqlSessionFactory = reactiveSqlSessionFactory;
    }

    @Override
    public <T> Mono<T> execute(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                               Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        monoExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Mono<T> executeAndCommit(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                        Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        monoExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> reactiveSqlSession.rollback(true)
                        .then(Mono.defer(reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Mono<T> executeAndRollback(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                          Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        monoExecution,
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> reactiveSqlSession.rollback(true)
                        .then(Mono.defer(reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeMany(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                   Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        fluxExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeManyAndCommit(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                            Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        fluxExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> reactiveSqlSession.rollback(true)
                        .then(Mono.defer(reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    @Override
    public <T> Flux<T> executeManyAndRollback(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                              Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        fluxExecution,
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> reactiveSqlSession.rollback(true)
                        .then(Mono.defer(reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(((MybatisReactiveContextManager) reactiveSqlSession)::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }
}

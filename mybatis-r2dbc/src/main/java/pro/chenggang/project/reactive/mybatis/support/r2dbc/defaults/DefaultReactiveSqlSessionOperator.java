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

import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

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
    public <T> Flux<T> executeThenClose(final ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                        BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Publisher<T>> execution) {
        final ReactiveSqlSession reactiveSqlSession = this.reactiveSqlSessionFactory.openSession(
                reactiveSqlSessionProfile);
        return MybatisReactiveContextManager.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux
                        .usingWhen(
                                Mono.just(reactiveSqlSession),
                                currentReactiveSqlSession -> execution.apply(currentReactiveSqlSession,
                                        currentReactiveSqlSession.getProfile()
                                ),
                                currentReactiveSqlSession -> Mono.defer(
                                                () -> {
                                                    if (currentReactiveSqlSession.getProfile().isForceToRollback()) {
                                                        return currentReactiveSqlSession.rollback(true);
                                                    } else {
                                                        return currentReactiveSqlSession.commit(true);
                                                    }
                                                })
                                        .then(Mono.defer(currentReactiveSqlSession::close)),
                                (currentReactiveSqlSession, err) -> currentReactiveSqlSession.rollback(true)
                                        .then(Mono.defer(currentReactiveSqlSession::close)),
                                currentReactiveSqlSession -> currentReactiveSqlSession.rollback(true)
                                        .then(Mono.defer(currentReactiveSqlSession::close))
                                        .onErrorMap(this::unwrapIfResourceCleanupFailure)
                        )
                )
                .contextWrite(reactiveSqlSession::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    /**
     * Unwrap the cause of a throwable, if produced by a failure
     * during the async resource cleanup in {@link Flux#usingWhen}.
     *
     * @param ex the throwable to try to unwrap
     */
    private Throwable unwrapIfResourceCleanupFailure(Throwable ex) {
        if (ex instanceof RuntimeException && ex.getCause() != null) {
            String msg = ex.getMessage();
            if (msg != null && msg.startsWith("Async resource cleanup failed")) {
                return ex.getCause();
            }
        }
        return ex;
    }
}

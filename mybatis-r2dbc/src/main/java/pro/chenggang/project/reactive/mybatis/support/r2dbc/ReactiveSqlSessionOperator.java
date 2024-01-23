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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession.DEFAULT_PROFILE;

/**
 * The interface Reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSessionOperator {

    /**
     * Execute then close.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param execution                 the execution
     * @return the flux
     */
    <T> Flux<T> executeThenClose(final ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                 BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Publisher<T>> execution);

    /**
     * Execute mono then close.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param execution                 the execution
     * @return the mono
     */
    default <T> Mono<T> executeMonoThenClose(final ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                             BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Mono<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfile, execution::apply).singleOrEmpty();
    }

    /**
     * Execute flux then close.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param execution                 the execution
     * @return the flux
     */
    default <T> Flux<T> executeFluxThenClose(final ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                             BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Flux<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfile, execution::apply);
    }

    /**
     * Execute then close with default reactive sql session profile.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>       the type parameter
     * @param execution the execution
     * @return the flux
     */
    default <T> Flux<T> executeThenClose(BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Publisher<T>> execution) {
        return executeThenClose(DEFAULT_PROFILE, execution);
    }

    /**
     * Execute mono then close with default reactive sql session profile.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>       the type parameter
     * @param execution the execution
     * @return the mono
     */
    default <T> Mono<T> executeMonoThenClose(BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Mono<T>> execution) {
        return executeThenClose(DEFAULT_PROFILE, execution::apply).singleOrEmpty();
    }

    /**
     * Execute flux then close with default reactive sql session profile.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>       the type parameter
     * @param execution the execution
     * @return the flux
     */
    default <T> Flux<T> executeFluxThenClose(BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Flux<T>> execution) {
        return executeThenClose(DEFAULT_PROFILE, execution::apply);
    }

    /**
     * Execute then close with given reactive sql session.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                the type parameter
     * @param reactiveSqlSession the reactive sql session
     * @param execution          the execution
     * @return the flux
     */
    static <T> Flux<T> executeThenClose(final ReactiveSqlSession reactiveSqlSession,
                                        BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Publisher<T>> execution) {
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
                                        .onErrorMap(throwable -> {
                                            if (throwable instanceof RuntimeException && throwable.getCause() != null) {
                                                String msg = throwable.getMessage();
                                                if (msg != null && msg.startsWith("Async resource cleanup failed")) {
                                                    return throwable.getCause();
                                                }
                                            }
                                            return throwable;
                                        })
                        )
                )
                .contextWrite(reactiveSqlSession::initReactiveExecutorContext)
                .contextWrite(MybatisReactiveContextManager::initReactiveExecutorContextAttribute);
    }

    /**
     * Execute mono then close with given reactive sql session.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                the type parameter
     * @param reactiveSqlSession the reactive sql session
     * @param execution          the execution
     * @return the mono
     */
    static <T> Mono<T> executeMonoThenClose(final ReactiveSqlSession reactiveSqlSession,
                                            BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Mono<T>> execution) {
        return executeThenClose(reactiveSqlSession, execution::apply).singleOrEmpty();
    }

    /**
     * Execute flux then close with given reactive sql session.
     * Configure {@code reactiveSqlSessionProfile.forceToRollback()} to require a rollback operation in BiFunction named as execution.
     *
     * @param <T>                the type parameter
     * @param reactiveSqlSession the reactive sql session
     * @param execution          the execution
     * @return the flux
     */
    static <T> Flux<T> executeFluxThenClose(final ReactiveSqlSession reactiveSqlSession,
                                            BiFunction<ReactiveSqlSession, ReactiveSqlSessionProfile, Flux<T>> execution) {
        return executeThenClose(reactiveSqlSession, execution::apply);
    }

}

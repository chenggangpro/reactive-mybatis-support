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

import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession.DEFAULT_PROFILE;

/**
 * The interface Reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSessionOperator {

    /**
     * execute with Mono
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param monoExecution             the mono execution
     * @return mono
     */
    <T> Mono<T> execute(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                        Function<ReactiveSqlSession, Mono<T>> monoExecution);

    /**
     * execute with Mono
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    default <T> Mono<T> execute(Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        return execute(DEFAULT_PROFILE, monoExecution);
    }

    /**
     * execute with Mono then commit
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param monoExecution             the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndCommit(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                 Function<ReactiveSqlSession, Mono<T>> monoExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    default <T> Mono<T> executeAndCommit(Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        return executeAndCommit(DEFAULT_PROFILE, monoExecution);
    }

    /**
     * execute with Mono then rollback
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param monoExecution             the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndRollback(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                   Function<ReactiveSqlSession, Mono<T>> monoExecution);

    /**
     * execute with Mono then rollback
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return the mono
     */
    default <T> Mono<T> executeAndRollback(Function<ReactiveSqlSession, Mono<T>> monoExecution) {
        return executeAndRollback(DEFAULT_PROFILE, monoExecution);
    }

    /**
     * execute with Mono then commit
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param fluxExecution             the flux execution
     * @return flux
     */
    <T> Flux<T> executeMany(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                            Function<ReactiveSqlSession, Flux<T>> fluxExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    default <T> Flux<T> executeMany(Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        return executeMany(DEFAULT_PROFILE, fluxExecution);
    }

    /**
     * execute with Flux
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param fluxExecution             the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndCommit(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                     Function<ReactiveSqlSession, Flux<T>> fluxExecution);

    /**
     * execute with Flux
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    default <T> Flux<T> executeManyAndCommit(Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        return executeManyAndCommit(DEFAULT_PROFILE, fluxExecution);
    }

    /**
     * execute with Flux then rollback
     *
     * @param <T>                       the type parameter
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @param fluxExecution             the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndRollback(ReactiveSqlSessionProfile reactiveSqlSessionProfile,
                                       Function<ReactiveSqlSession, Flux<T>> fluxExecution);

    /**
     * execute with Flux then rollback
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    default <T> Flux<T> executeManyAndRollback(Function<ReactiveSqlSession, Flux<T>> fluxExecution) {
        return executeManyAndRollback(DEFAULT_PROFILE, fluxExecution);
    }

}

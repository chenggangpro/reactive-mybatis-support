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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile.ReactiveSqlSessionProfileBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Operator interface for executing reactive SQL session operations with automatic resource management.
 * This interface provides methods to execute database operations within a reactive SQL session context
 * and ensures proper cleanup after execution completes.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSessionOperator {

    /**
     * Executes a database operation within a reactive SQL session and automatically closes the session after completion.
     * This is the base method that all other execution methods delegate to.
     *
     * @param <T>                       the type of elements emitted by the resulting Flux
     * @param reactiveSqlSessionProfile the profile configuration for the reactive SQL session
     * @param execution                 the function that performs the database operation using the provided ReactiveSqlSession
     * @return a Flux emitting the results of the execution
     */
    <T> Flux<T> executeThenClose(ReactiveSqlSessionProfile reactiveSqlSessionProfile, Function<ReactiveSqlSession, Publisher<T>> execution);

    /**
     * Executes a database operation that returns a single result within a reactive SQL session and automatically closes the session.
     * If the execution produces multiple results, only the first one is returned. If no results are produced, an empty Mono is returned.
     *
     * @param <T>                       the type of element emitted by the resulting Mono
     * @param reactiveSqlSessionProfile the profile configuration for the reactive SQL session
     * @param execution                 the function that performs the database operation using the provided ReactiveSqlSession and returns a Mono
     * @return a Mono emitting the single result of the execution, or empty if no result is produced
     */
    default <T> Mono<T> executeMonoThenClose(ReactiveSqlSessionProfile reactiveSqlSessionProfile, Function<ReactiveSqlSession, Mono<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfile, execution::apply).singleOrEmpty();
    }

    /**
     * Executes a database operation that returns multiple results within a reactive SQL session and automatically closes the session.
     *
     * @param <T>                       the type of elements emitted by the resulting Flux
     * @param reactiveSqlSessionProfile the profile configuration for the reactive SQL session
     * @param execution                 the function that performs the database operation using the provided ReactiveSqlSession and returns a Flux
     * @return a Flux emitting the results of the execution
     */
    default <T> Flux<T> executeFluxThenClose(ReactiveSqlSessionProfile reactiveSqlSessionProfile, Function<ReactiveSqlSession, Flux<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfile, execution::apply);
    }

    /**
     * Executes a database operation within a reactive SQL session configured via a builder consumer and automatically closes the session.
     * This method allows for inline configuration of the session profile.
     *
     * @param <T>                                the type of elements emitted by the resulting Flux
     * @param reactiveSqlSessionProfileConfigure a consumer that configures the ReactiveSqlSessionProfile.Builder
     * @param execution                          the function that performs the database operation using the provided ReactiveSqlSession
     * @return a Flux emitting the results of the execution
     */
    default <T> Flux<T> executeThenClose(Consumer<ReactiveSqlSessionProfileBuilder> reactiveSqlSessionProfileConfigure, Function<ReactiveSqlSession, Publisher<T>> execution) {
        ReactiveSqlSessionProfileBuilder reactiveSqlSessionProfileBuilder = ReactiveSqlSessionProfile.builder();
        reactiveSqlSessionProfileConfigure.accept(reactiveSqlSessionProfileBuilder);
        return executeThenClose(reactiveSqlSessionProfileBuilder.build(), execution);
    }

    /**
     * Executes a database operation that returns a single result within a reactive SQL session configured via a builder consumer
     * and automatically closes the session. If the execution produces multiple results, only the first one is returned.
     * If no results are produced, an empty Mono is returned.
     *
     * @param <T>                                the type of element emitted by the resulting Mono
     * @param reactiveSqlSessionProfileConfigure a consumer that configures the ReactiveSqlSessionProfile.Builder
     * @param execution                          the function that performs the database operation using the provided ReactiveSqlSession and returns a Mono
     * @return a Mono emitting the single result of the execution, or empty if no result is produced
     */
    default <T> Mono<T> executeMonoThenClose(Consumer<ReactiveSqlSessionProfileBuilder> reactiveSqlSessionProfileConfigure, Function<ReactiveSqlSession, Mono<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfileConfigure, execution::apply).singleOrEmpty();
    }

    /**
     * Executes a database operation that returns multiple results within a reactive SQL session configured via a builder consumer
     * and automatically closes the session.
     *
     * @param <T>                                the type of elements emitted by the resulting Flux
     * @param reactiveSqlSessionProfileConfigure a consumer that configures the ReactiveSqlSessionProfile.Builder
     * @param execution                          the function that performs the database operation using the provided ReactiveSqlSession and returns a Flux
     * @return a Flux emitting the results of the execution
     */
    default <T> Flux<T> executeFluxThenClose(Consumer<ReactiveSqlSessionProfileBuilder> reactiveSqlSessionProfileConfigure, Function<ReactiveSqlSession, Flux<T>> execution) {
        return executeThenClose(reactiveSqlSessionProfileConfigure, execution::apply);
    }

}

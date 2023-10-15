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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface ReactiveSqlSessionOperator {

    /**
     * execute with Mono
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> execute(Mono<T> monoExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndCommit(Mono<T> monoExecution);

    /**
     * execute with Mono then rollback
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndRollback(Mono<T> monoExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeMany(Flux<T> fluxExecution);

    /**
     * execute with Flux
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndCommit(Flux<T> fluxExecution);

    /**
     * execute with Flux then rollback
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndRollback(Flux<T> fluxExecution);

}

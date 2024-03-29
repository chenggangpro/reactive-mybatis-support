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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler;

import io.r2dbc.spi.Readable;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive result handler.
 *
 * @author Gang Cheng
 * @version 1.0.10
 * @since 1.0.0
 */
public interface ReactiveResultHandler {

    /**
     * get result row total count
     *
     * @return result row total count
     */
    Integer getResultRowTotalCount();

    /**
     * handle result with RowResultWrapper
     *
     * @param <T>              the type parameter
     * @param readableResultWrapper the row result wrapper
     * @return list
     */
    <T> Mono<T> handleResult(ReadableResultWrapper<? extends Readable> readableResultWrapper);

    /**
     * Handle output parameters.
     *
     * @param readableResultWrapper the row result wrapper
     */
    <T> Mono<T> handleOutputParameters(ReadableResultWrapper<? extends Readable> readableResultWrapper);

    /**
     * get remained result or empty list
     *
     * @param <T> the type parameter
     * @return remained results
     */
    <T> Flux<T> getRemainedResults();

    /**
     * Clean up
     */
    void cleanup();
}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.util.Optional;
import java.util.Set;

/**
 * The Placeholder dialect registry.
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderDialectRegistry {

    /**
     * Register PlaceholderDialect
     *
     * @param placeholderDialect the placeholder dialect
     */
    void register(PlaceholderDialect placeholderDialect);

    /**
     * Get all PlaceholderDialect type
     * @return all PlaceholderDialect's type set
     */
    Set<Class<? extends PlaceholderDialect>> getAllPlaceholderDialectTypes();

    /**
     * Gets placeholder dialect.
     *
     * @param connectionMetadata                the connection metadata
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the placeholder dialect
     */
    Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionMetadata connectionMetadata, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute);
}

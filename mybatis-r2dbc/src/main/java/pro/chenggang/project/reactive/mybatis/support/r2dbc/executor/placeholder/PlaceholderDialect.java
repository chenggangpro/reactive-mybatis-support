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

import java.util.Locale;
import java.util.Optional;

/**
 * Placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderDialect {

    /**
     * The constant PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY.
     */
    String PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY = "PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY";

    /**
     * The constant DEFAULT_PLACEHOLDER
     */
    String DEFAULT_PLACEHOLDER = "?";

    /**
     * Dialect name.
     *
     * @return the dialect name
     */
    String name();

    /**
     * Supported boolean.
     *
     * @param connectionMetadata               the connection metadata
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the boolean
     */
    default boolean supported(ConnectionMetadata connectionMetadata,
                              ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        String name = Optional.ofNullable(reactiveExecutorContextAttribute
                        .getAttribute()
                        .get(PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY)
                )
                .filter(value -> value instanceof String)
                .map(String.class::cast)
                .orElseGet(connectionMetadata::getDatabaseProductName);
        return name.equalsIgnoreCase(this.name())
                || name.toLowerCase(Locale.ENGLISH).contains(this.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get marker string.
     *
     * @return the marker
     */
    default String getMarker() {
        return DEFAULT_PLACEHOLDER;
    }

    /**
     * Using index marker.
     *
     * @return the boolean
     */
    default boolean usingIndexMarker() {
        return true;
    }

    /**
     * Placeholder start index.
     *
     * @return the int default is 0
     */
    default int startIndex() {
        return 0;
    }

    /**
     * Property name post process .
     *
     * @param propertyName the property name
     * @return the processed property name
     */
    default String propertyNamePostProcess(String propertyName) {
        return propertyName;
    }
}

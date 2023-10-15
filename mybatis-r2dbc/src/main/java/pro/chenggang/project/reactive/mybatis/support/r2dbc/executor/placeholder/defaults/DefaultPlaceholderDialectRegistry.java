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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults;

import io.r2dbc.spi.ConnectionMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.H2PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.MariaDBPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.MySQLPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.OraclePlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.PostgreSQLPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.SQLServerPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect.PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY;

/**
 * Default placeholder dialect factory
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class DefaultPlaceholderDialectRegistry implements PlaceholderDialectRegistry {

    private final Map<String, PlaceholderDialect> placeholderDialects = new HashMap<>();

    public DefaultPlaceholderDialectRegistry() {
        this.register(new MySQLPlaceholderDialect());
        this.register(new MariaDBPlaceholderDialect());
        this.register(new PostgreSQLPlaceholderDialect());
        this.register(new H2PlaceholderDialect());
        this.register(new OraclePlaceholderDialect());
        this.register(new SQLServerPlaceholderDialect());
    }

    @Override
    public void register(PlaceholderDialect placeholderDialect) {
        this.placeholderDialects.put(placeholderDialect.name().toLowerCase(Locale.ENGLISH), placeholderDialect);
    }

    @Override
    public Set<Class<? extends PlaceholderDialect>> getAllPlaceholderDialectTypes() {
        return this.placeholderDialects.values()
                .stream()
                .map(PlaceholderDialect::getClass)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionMetadata connectionMetadata, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        String name = Optional.ofNullable(reactiveExecutorContextAttribute.getAttribute().get(PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY))
                .filter(value -> value instanceof String)
                .map(String.class::cast)
                .orElseGet(connectionMetadata::getDatabaseProductName);
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        if (this.placeholderDialects.containsKey(lowerCaseName)) {
            return Optional.of(this.placeholderDialects.get(lowerCaseName));
        }
        return this.placeholderDialects
                .values()
                .stream()
                .filter(placeholderDialect -> placeholderDialect.supported(connectionMetadata,reactiveExecutorContextAttribute))
                .findFirst();
    }
}

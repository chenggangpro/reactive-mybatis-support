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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;

/**
 * PostgreSQL placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class PostgreSQLPlaceholderDialect implements PlaceholderDialect {

    /**
     * The dialect name
     */
    public static final String DIALECT_NAME = "PostgreSQL";

    @Override
    public String name() {
        return DIALECT_NAME;
    }

    @Override
    public String getMarker() {
        return "$";
    }

    @Override
    public int startIndex() {
        return 1;
    }
}

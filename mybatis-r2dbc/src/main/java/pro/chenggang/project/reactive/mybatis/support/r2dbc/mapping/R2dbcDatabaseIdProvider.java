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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Properties;

/**
 * Should return an id to identify the type of this database. That id can be used later on to build different queries
 * for each database type This mechanism enables supporting multiple vendors or versions
 *
 * @author Eduardo Macarron
 * @author Gang Cheng
 * @see org.apache.ibatis.mapping.DatabaseIdProvider
 */
public interface R2dbcDatabaseIdProvider {

    default void setProperties(Properties p) {
        // NOP
    }

    String getDatabaseId(ConnectionFactory connectionFactory);
}

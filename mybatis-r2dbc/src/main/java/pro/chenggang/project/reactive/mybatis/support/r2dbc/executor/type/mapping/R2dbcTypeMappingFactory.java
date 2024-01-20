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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.mapping;

import io.r2dbc.spi.R2dbcType;
import org.apache.ibatis.type.JdbcType;

/**
 * The r2dbc type mapping factory
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public interface R2dbcTypeMappingFactory {

    /**
     * Register or replace JdbcType to R2dbcType 's mapping relationship.
     *
     * @param jdbcType  the jdbc type
     * @param r2dbcType the r2dbc type
     */
    void registerOrReplace(JdbcType jdbcType, R2dbcType r2dbcType);

    /**
     * Mapping JdbcType to R2dbcType.
     *
     * @param jdbcType the jdbc type
     * @return the r2dbc type could be null if no mapping exist or jdbcType is null
     */
    R2dbcType mapping(JdbcType jdbcType);

}

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
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The default r2dbc type mapping
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class DefaultR2DbcTypeMappingFactory implements R2dbcTypeMappingFactory {

    protected final Map<JdbcType, R2dbcType> mappings = new HashMap<>();


    public DefaultR2DbcTypeMappingFactory() {
        this.initializeDefaultMappingByName();
    }

    /**
     * Initialize default mapping by name.
     */
    protected void initializeDefaultMappingByName() {
        Arrays.stream(JdbcType.values())
                .map(jdbcType -> {
                    try {
                        return Tuples.of(jdbcType, R2dbcType.valueOf(jdbcType.name()));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(tuple2 -> this.mappings.put(tuple2.getT1(), tuple2.getT2()));
    }

    @Override
    public void registerOrReplace(JdbcType jdbcType, R2dbcType r2dbcType) {
        mappings.put(jdbcType, r2dbcType);
    }

    @Override
    public R2dbcType mapping(JdbcType jdbcType) {
        return Objects.isNull(jdbcType) ? null : this.mappings.get(jdbcType);
    }
}

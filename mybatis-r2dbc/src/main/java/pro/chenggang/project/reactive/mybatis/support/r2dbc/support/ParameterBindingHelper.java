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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.support;

import io.r2dbc.spi.Parameter;
import io.r2dbc.spi.Parameters;
import io.r2dbc.spi.R2dbcType;
import org.apache.ibatis.mapping.ParameterMode;

import java.util.Objects;

/**
 * The parameter type support
 *
 * @author Gang cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public abstract class ParameterBindingHelper {

    /**
     * To parameter.
     *
     * @param parameterMode the parameter mode
     * @param javaType      the java type
     * @param value         the value
     * @return the parameter
     */
    public static Parameter toParameter(ParameterMode parameterMode,
                                        Class<?> javaType,
                                        R2dbcType r2dbcType,
                                        Object value) {
        Objects.requireNonNull(parameterMode, "Parameter Mode can not be null ");
        Objects.requireNonNull(javaType, "Parameter Java Type can not be null ");
        switch (parameterMode) {
            case OUT:
                return Objects.nonNull(r2dbcType) ? Parameters.out(r2dbcType) : Parameters.out(javaType);
            case INOUT:
                if (Objects.isNull(value)) {
                    return Objects.nonNull(r2dbcType) ? Parameters.inOut(r2dbcType) : Parameters.inOut(javaType);
                }
                return Objects.nonNull(r2dbcType) ? Parameters.inOut(r2dbcType, value) : Parameters.inOut(value);
            case IN:
                if (Objects.isNull(value)) {
                    return Objects.nonNull(r2dbcType) ? Parameters.in(r2dbcType) : Parameters.in(javaType);
                }
                return Objects.nonNull(r2dbcType) ? Parameters.in(r2dbcType, value) : Parameters.in(value);
            default:
                throw new IllegalArgumentException("Unrecognized parameter mode : " + parameterMode);
        }
    }
}

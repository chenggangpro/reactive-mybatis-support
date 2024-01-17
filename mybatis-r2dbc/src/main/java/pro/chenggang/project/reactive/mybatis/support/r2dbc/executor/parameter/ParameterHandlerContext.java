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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter;

import io.r2dbc.spi.R2dbcType;
import org.apache.ibatis.mapping.ParameterMode;

/**
 * The type Parameter handler context.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ParameterHandlerContext {

    private int index;
    private R2dbcType r2dbcType;
    private Class<?> javaType;
    private ParameterMode parameterMode;


    /**
     * Gets index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets r2dbc type.
     *
     * @return the r2dbc type
     */
    public R2dbcType getR2dbcType() {
        return r2dbcType;
    }

    /**
     * Sets r2dbc type.
     *
     * @param r2dbcType the r2dbc type
     */
    public void setR2dbcType(R2dbcType r2dbcType) {
        this.r2dbcType = r2dbcType;
    }

    /**
     * Gets java type.
     *
     * @return the java type
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Sets java type.
     *
     * @param javaType the java type
     */
    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /**
     * Gets parameter mode.
     *
     * @return the parameter mode
     */
    public ParameterMode getParameterMode() {
        return parameterMode;
    }

    /**
     * Sets parameter mode.
     *
     * @param parameterMode the parameter mode
     */
    public void setParameterMode(ParameterMode parameterMode) {
        this.parameterMode = parameterMode;
    }
}

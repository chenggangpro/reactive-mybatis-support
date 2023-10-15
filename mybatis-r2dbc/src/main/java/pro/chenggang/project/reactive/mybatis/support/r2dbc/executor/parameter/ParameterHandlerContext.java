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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter;

import org.apache.ibatis.type.JdbcType;

/**
 * The type Parameter handler context.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ParameterHandlerContext {

    private int index;
    private JdbcType jdbcType;
    private Class<?> javaType;


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
     * Gets jdbc type.
     *
     * @return the jdbc type
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Sets jdbc type.
     *
     * @param jdbcType the jdbc type
     */
    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
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
}

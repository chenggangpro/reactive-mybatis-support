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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;

/**
 * The interface R2dbc type handler adapter.
 *
 * @param <T> the type parameter
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface R2dbcTypeHandlerAdapter<T> {

    /**
     * adapted class
     *
     * @return class
     */
    Class<T> adaptClazz();

    /**
     * setParameter
     *
     * @param statement               the statement
     * @param parameterHandlerContext the parameter handler context
     * @param parameter               the parameter
     */
    void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, T parameter);

    /**
     * get result by columnName
     *
     * @param row         the row
     * @param rowMetadata the row metadata
     * @param columnName  the column name
     * @return result
     */
    T getResult(Row row, RowMetadata rowMetadata, String columnName);

    /**
     * get result by columnIndex
     *
     * @param row         the row
     * @param rowMetadata the row metadata
     * @param columnIndex the column index
     * @return result
     */
    T getResult(Row row, RowMetadata rowMetadata, int columnIndex);
}

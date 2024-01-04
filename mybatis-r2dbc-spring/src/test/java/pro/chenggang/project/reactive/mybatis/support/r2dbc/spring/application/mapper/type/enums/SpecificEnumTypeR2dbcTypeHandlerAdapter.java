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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.enums;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

public class SpecificEnumTypeR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<SpecificEnumType> {

    @Override
    public Class<SpecificEnumType> adaptClazz() {
        return SpecificEnumType.class;
    }

    @Override
    public void setParameter(Statement statement,
                             ParameterHandlerContext parameterHandlerContext,
                             SpecificEnumType parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.getValue());
    }

    @Override
    public SpecificEnumType getResult(Row row, RowMetadata rowMetadata, String columnName) {
        Integer result = row.get(columnName, Integer.class);
        return result == null ? null : SpecificEnumType.fromValue(result);
    }

    @Override
    public SpecificEnumType getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        Integer result = row.get(columnIndex, Integer.class);
        return result == null ? null : SpecificEnumType.fromValue(result);
    }
}
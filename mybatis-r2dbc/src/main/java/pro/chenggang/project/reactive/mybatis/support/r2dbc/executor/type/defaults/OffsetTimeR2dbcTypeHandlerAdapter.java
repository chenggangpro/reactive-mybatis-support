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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * The type Offset time r2dbc type handler adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class OffsetTimeR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<OffsetTime> {

    @Override
    public Class<OffsetTime> adaptClazz() {
        return OffsetTime.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, OffsetTime parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.toLocalTime());
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalTime localTime = row.get(columnName, LocalTime.class);
        if (null == localTime) {
            return null;
        }
        return OffsetTime.from(localTime);
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalTime localTime = row.get(columnIndex, LocalTime.class);
        if (null == localTime) {
            return null;
        }
        return OffsetTime.from(localTime);
    }

}

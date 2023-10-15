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

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * The type Timestamp r2dbc type handler adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class TimestampR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<Timestamp> {

    @Override
    public Class<Timestamp> adaptClazz() {
        return Timestamp.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, Timestamp parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.toLocalDateTime());
    }

    @Override
    public Timestamp getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalDateTime localDateTime = row.get(columnName, LocalDateTime.class);
        if (null == localDateTime) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    @Override
    public Timestamp getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalDateTime localDateTime = row.get(columnIndex, LocalDateTime.class);
        if (null == localDateTime) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

}

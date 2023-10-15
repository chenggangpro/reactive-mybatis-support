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

import java.nio.ByteBuffer;

/**
 * The type Byte array r2dbc type handler adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ByteArrayR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<byte[]> {

    @Override
    public Class<byte[]> adaptClazz() {
        return byte[].class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, byte[] parameter) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(parameter);
        statement.bind(parameterHandlerContext.getIndex(), byteBuffer);
    }

    @Override
    public byte[] getResult(Row row, RowMetadata rowMetadata, String columnName) {
        ByteBuffer byteBuffer = row.get(columnName, ByteBuffer.class);
        if (null == byteBuffer) {
            return null;
        }
        return byteBuffer.array();
    }

    @Override
    public byte[] getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        ByteBuffer byteBuffer = row.get(columnIndex, ByteBuffer.class);
        if (null == byteBuffer) {
            return null;
        }
        return byteBuffer.array();
    }

}

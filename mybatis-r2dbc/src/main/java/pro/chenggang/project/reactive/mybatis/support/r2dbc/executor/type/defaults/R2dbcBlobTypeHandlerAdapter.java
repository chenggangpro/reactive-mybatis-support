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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Readable;
import io.r2dbc.spi.ReadableMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class R2dbcBlobTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<Blob> {

    @Override
    public Class<Blob> adaptClazz() {
        return Blob.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, Blob parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter);
    }

    @Override
    public Blob getResult(Readable readable, ReadableMetadata readableMetadata, String columnName) {
        return readable.get(columnName, Blob.class);
    }

    @Override
    public Blob getResult(Readable readable, ReadableMetadata readableMetadata, int columnIndex) {
        return readable.get(columnIndex, Blob.class);
    }
}

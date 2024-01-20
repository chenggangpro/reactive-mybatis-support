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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result;

import io.r2dbc.spi.Readable;
import org.apache.ibatis.type.TypeHandler;

/**
 * The interface Type handle context.
 *
 * @author Gang Cheng
 * @version 2.0.0
 * @since 1.0.0
 */
public interface TypeHandleContext {

    /**
     * Set delegated type handler
     *
     * @param targetType           the target type
     * @param delegatedTypeHandler the delegated type handler
     * @param readableResultWrapper     the row result wrapper
     */
    void contextWith(Class<?> targetType, TypeHandler<?> delegatedTypeHandler, ReadableResultWrapper<? extends Readable> readableResultWrapper);

}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.converter;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.EnumOrdinalR2dbcTypeHandlerAdapter;

import java.lang.reflect.Field;

/**
 * The enum ordinal type handler converter
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class EnumOrdinalMybatisTypeHandlerConverter implements MybatisTypeHandlerConverter {
    
    @Override
    public boolean shouldConvert(TypeHandler<?> originalMybatisTypeHandler) {
        return originalMybatisTypeHandler instanceof EnumOrdinalTypeHandler;
    }

    @Override
    public R2dbcTypeHandlerAdapter<?> convert(TypeHandler<?> originalMybatisTypeHandler) {
        EnumOrdinalTypeHandler<?> enumOrdinalTypeHandler = (EnumOrdinalTypeHandler<?>) originalMybatisTypeHandler;
        try {
            Field declaredField = enumOrdinalTypeHandler.getClass()
                    .getDeclaredField("type");
            declaredField.setAccessible(true);
            Class<Enum<?>> type = (Class<Enum<?>>) declaredField.get(enumOrdinalTypeHandler);
            return new EnumOrdinalR2dbcTypeHandlerAdapter(type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

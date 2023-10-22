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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeReference;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.CallableStatement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The type Delegate R2dbc result row data handler.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class DelegateR2dbcResultRowDataHandler implements InvocationHandler {

    private static final Log log = LogFactory.getLog(DelegateR2dbcResultRowDataHandler.class);
    private final Set<Class<?>> notSupportedDataTypes;
    private final R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry;
    private TypeHandler<?> delegatedTypeHandler;
    private RowResultWrapper rowResultWrapper;
    private Class<?> targetType;

    /**
     * Instantiates a new Delegate R2dbc result row data handler.
     *
     * @param notSupportedDataTypes    the not supported data types
     * @param r2dbcTypeHandlerAdapterRegistry the R2dbc type handler adapter registry
     */
    public DelegateR2dbcResultRowDataHandler(Set<Class<?>> notSupportedDataTypes,
                                             R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry) {
        this.notSupportedDataTypes = notSupportedDataTypes;
        this.r2dbcTypeHandlerAdapterRegistry = r2dbcTypeHandlerAdapterRegistry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("contextWith".equals(method.getName())) {
            this.delegatedTypeHandler = (TypeHandler<?>) args[1];
            this.rowResultWrapper = (RowResultWrapper) args[2];
            Optional<Class<?>> typeHandlerArgumentType = this.getTypeHandlerArgumentType(delegatedTypeHandler);
            if(!typeHandlerArgumentType.isPresent()){
                this.targetType = (Class<?>) args[0];
            }else {
                this.targetType = typeHandlerArgumentType.get();
            }
            return null;
        }
        //not getResult() method ,return original invocation
        if (!"getResult".equals(method.getName())) {
            return method.invoke(delegatedTypeHandler, args);
        }
        Object firstArg = args[0];
        Object secondArg = args[1];
        if (null == secondArg) {
            return method.invoke(delegatedTypeHandler, args);
        }
        if (firstArg instanceof CallableStatement) {
            return method.invoke(delegatedTypeHandler, args);
        }
        //not supported
        if (notSupportedDataTypes.contains(this.targetType)) {
            throw new IllegalArgumentException("Unsupported Result Data type : " + targetType);
        }
        //using adapter
        if (r2dbcTypeHandlerAdapterRegistry.hasR2dbcTypeHandlerAdapter(this.targetType)) {
            log.trace("Found r2dbc type handler adapter fro result type : " + this.targetType);
            R2dbcTypeHandlerAdapter<?> r2dbcTypeHandlerAdapter = r2dbcTypeHandlerAdapterRegistry.getR2dbcTypeHandlerAdapter(this.targetType);
            // T getResult(ResultSet rs, String columnName)
            if (secondArg instanceof String) {
                return r2dbcTypeHandlerAdapter.getResult(rowResultWrapper.getRow(), rowResultWrapper.getRowMetadata(), (String) secondArg);
            }
            // T getResult(ResultSet rs, int columnIndex)
            if (secondArg instanceof Integer) {
                return r2dbcTypeHandlerAdapter.getResult(rowResultWrapper.getRow(), rowResultWrapper.getRowMetadata(), (Integer) secondArg - 1);
            }
        }
        // T getResult(ResultSet rs, String columnName)
        if (secondArg instanceof String) {
            return rowResultWrapper.getRow().get((String) secondArg, targetType);
        }
        // T getResult(ResultSet rs, int columnIndex)
        if (secondArg instanceof Integer) {
            return rowResultWrapper.getRow().get((Integer) secondArg - 1, targetType);
        }
        return null;
    }

    /**
     * get type handler actual type argument
     *
     * @return Optional class
     */
    private Optional<Class<?>> getTypeHandlerArgumentType(TypeHandler<?> typeHandler) {
        if (typeHandler instanceof TypeReference) {
            TypeReference<?> typeReference = (TypeReference<?>) typeHandler;
            return Optional.ofNullable(this.extraType(typeReference.getRawType()));
        }
        return Stream.of(typeHandler.getClass().getGenericSuperclass())
                .filter(type -> type instanceof ParameterizedType)
                .map(ParameterizedType.class::cast)
                .filter(parameterizedType -> TypeHandler.class.isAssignableFrom((Class<?>) (parameterizedType.getRawType())))
                .flatMap(parameterizedType -> Stream.of(parameterizedType.getActualTypeArguments()))
                .findFirst()
                .map(this::extraType);
    }

    private Class<?> extraType(Type type){
        if(type instanceof Class){
            return (Class<?>) type;
        }
        if(type instanceof TypeVariable){
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            Type[] bounds = typeVariable.getBounds();
            if(bounds.length > 0){
                Type firstBound = bounds[0];
                if(firstBound instanceof Class){
                    return (Class<?>) firstBound;
                }
            }
        }
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if(actualTypeArguments.length > 0){
                Type actualTypeArgument = actualTypeArguments[0];
                if(actualTypeArgument instanceof Class){
                    return (Class<?>) actualTypeArgument;
                }
            }
        }
        return null;
    }

}

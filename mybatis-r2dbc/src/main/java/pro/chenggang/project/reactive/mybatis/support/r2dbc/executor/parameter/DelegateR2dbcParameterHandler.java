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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter;

import io.r2dbc.spi.R2dbcType;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.ibatis.mapping.ParameterMode.IN;
import static org.apache.ibatis.mapping.ParameterMode.OUT;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ParameterBindingHelper.toParameter;

/**
 * The type Delegate R2dbc parameter handler.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class DelegateR2dbcParameterHandler implements InvocationHandler {

    private static final Log log = LogFactory.getLog(DelegateR2dbcParameterHandler.class);
    private final R2dbcMybatisConfiguration configuration;
    private final ParameterHandler parameterHandler;
    private final Map<Class<?>, Field> parameterHandlerFieldMap;
    private final Statement delegateStatement;
    private final PreparedStatement delegatedPreparedStatement;
    private final AtomicReference<ParameterHandlerContext> parameterHandlerContextReference = new AtomicReference<>();
    private final R2dbcStatementLog r2dbcStatementLog;

    /**
     * Instantiates a new Delegate R2dbc parameter handler.
     *
     * @param r2dbcMybatisConfiguration the R2dbc mybatis configuration
     * @param parameterHandler          the parameter handler
     * @param statement                 the statement
     * @param r2dbcStatementLog         the statement log helper
     */
    public DelegateR2dbcParameterHandler(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                         ParameterHandler parameterHandler,
                                         Statement statement,
                                         R2dbcStatementLog r2dbcStatementLog) {
        this.configuration = r2dbcMybatisConfiguration;
        this.parameterHandler = parameterHandler;
        this.delegateStatement = statement;
        this.r2dbcStatementLog = r2dbcStatementLog;
        this.delegatedPreparedStatement = initDelegatedPreparedStatement();
        parameterHandlerFieldMap = Stream.of(parameterHandler.getClass().getDeclaredFields())
                .collect(Collectors.toMap(
                        Field::getType,
                        field -> {
                            field.setAccessible(true);
                            return field;
                        }
                ));
    }

    /**
     * init delegated prepared statement
     *
     * @return PreparedStatement
     */
    private PreparedStatement initDelegatedPreparedStatement() {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                PreparedStatement.class,
                () -> new DelegateR2dbcStatement(
                        this.delegateStatement,
                        this.configuration.getNotSupportedDataTypes()
                )
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (!Objects.equals("setParameters", methodName)) {
            return method.invoke(parameterHandler, args);
        }
        this.setParameters(this.delegatedPreparedStatement);
        return null;
    }

    /**
     * get field
     *
     * @param parameterHandler
     * @param fieldType
     * @param <T>
     * @return
     */
    private <T> T getField(ParameterHandler parameterHandler, Class<T> fieldType) {
        Field field = this.parameterHandlerFieldMap.get(fieldType);
        try {
            return (T) field.get(parameterHandler);
        } catch (IllegalAccessException e) {
            //ignore
        }
        return null;
    }

    /**
     * delegate set parameters
     *
     * @param ps the ps
     */
    public void setParameters(PreparedStatement ps) {
        BoundSql boundSql = this.getField(this.parameterHandler, BoundSql.class);
        TypeHandlerRegistry typeHandlerRegistry = this.getField(this.parameterHandler, TypeHandlerRegistry.class);
        R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry = configuration.getR2dbcTypeHandlerAdapterRegistry();
        Object parameterObject = parameterHandler.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        ParameterHandlerContext parameterHandlerContext = new ParameterHandlerContext();
        DelegateR2dbcParameterHandler.this.parameterHandlerContextReference.getAndSet(parameterHandlerContext);
        List<Object> columnValues = new ArrayList<>();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                ParameterMode parameterMode = parameterMapping.getMode();
                JdbcType jdbcType = parameterMapping.getJdbcType();
                if (OUT.equals(parameterMode)) {
                    R2dbcType r2dbcType = this.configuration.mappingR2dbcTypeFrom(jdbcType);
                    this.delegateStatement.bind(i,
                            toParameter(OUT, parameterMapping.getJavaType(), r2dbcType, null)
                    );
                    columnValues.add(null);
                    continue;
                }
                Object value;
                String propertyName = parameterMapping.getProperty();
                // issue #448 ask first for additional params
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                if (value == null && jdbcType == null) {
                    jdbcType = configuration.getJdbcTypeForNull();
                }
                R2dbcType r2dbcType = this.configuration.mappingR2dbcTypeFrom(jdbcType);
                try {
                    if (value == null) {
                        this.delegateStatement.bind(i,
                                toParameter(parameterMode, parameterMapping.getJavaType(), r2dbcType, null)
                        );
                        columnValues.add(null);
                        continue;
                    }
                    // if parameterMapping's javaType isn't Object.class
                    // then set it into context
                    // otherwise use value's class as javaType of context
                    Class<?> referredJavaType = Objects.equals(parameterMapping.getJavaType(), Object.class)
                            ? value.getClass() : parameterMapping.getJavaType();
                    parameterHandlerContext.setIndex(i);
                    parameterHandlerContext.setJavaType(referredJavaType);
                    parameterHandlerContext.setR2dbcType(r2dbcType);
                    parameterHandlerContext.setParameterMode(parameterMode);
                    if (r2dbcTypeHandlerAdapterRegistry.hasR2dbcTypeHandlerAdapter(value.getClass())) {
                        log.debug("Found r2dbc type handler adapter for type : " + value.getClass());
                        R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter = r2dbcTypeHandlerAdapterRegistry.getR2dbcTypeHandlerAdapter(
                                value.getClass());
                        r2dbcTypeHandlerAdapter.setParameter(delegateStatement, parameterHandlerContext, value);
                    } else {
                        TypeHandler typeHandler = parameterMapping.getTypeHandler();
                        typeHandler.setParameter(ps, i, value, jdbcType);
                    }
                    columnValues.add(value);
                } catch (TypeException | SQLException e) {
                    throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e,
                            e
                    );
                }
            }
        }
        r2dbcStatementLog.logParameters(columnValues);
    }


    /**
     * delegate Prepare statement
     */
    private class DelegateR2dbcStatement implements InvocationHandler {

        private final Statement statement;
        private final Set<Class<?>> notSupportedDataTypes;

        /**
         * Instantiates a new Delegate R2dbc statement.
         *
         * @param statement                the statement
         * @param notSupportedDataTypes    the not supported data types
         */
        DelegateR2dbcStatement(Statement statement,Set<Class<?>> notSupportedDataTypes) {
            this.statement = statement;
            this.notSupportedDataTypes = notSupportedDataTypes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("equals") || methodName.equals("hashCode") || methodName.equals("toString")) {
                return method.invoke(proxy, args);
            }
            if (!methodName.startsWith("set")) {
                //Does not handle non-set methods
                return null;
            }
            int index = (int) args[0];
            Object parameter = args[1];
            Class<?> parameterClass = parameter.getClass();
            //not supported types
            if (notSupportedDataTypes.contains(parameterClass)) {
                throw new IllegalArgumentException("Unsupported Parameter type : " + parameterClass);
            }
            ParameterHandlerContext parameterHandlerContext = DelegateR2dbcParameterHandler.this.parameterHandlerContextReference.get();
            // for r2dbc-mssql with 0.9 version of r2dbc-spi, there is a bug fixed with 1.0 version of r2dbc-spi
            // link source src/main/java/io/r2dbc/mssql/codec/DefaultCodecs.java#getServerType(Parameter parameter) in r2dbc-mssql
            if (IN.equals(parameterHandlerContext.getParameterMode())) {
                // if r2dbc type is not null ,then use binding with Parameter method
                // otherwise use binding with value method directly
                if(Objects.nonNull(parameterHandlerContext.getR2dbcType())){
                    statement.bind(index,
                            toParameter(parameterHandlerContext.getParameterMode(),
                                    parameterHandlerContext.getJavaType(),
                                    parameterHandlerContext.getR2dbcType(),
                                    parameter
                            )
                    );
                }else{
                    statement.bind(index, parameter);
                }
                return null;
            }
            //INOUT
            statement.bind(index,
                    toParameter(parameterHandlerContext.getParameterMode(),
                            parameterHandlerContext.getJavaType(),
                            parameterHandlerContext.getR2dbcType(),
                            parameter
                    )
            );
            return null;
        }

    }

}

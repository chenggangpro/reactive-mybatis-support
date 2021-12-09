package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter;

import io.r2dbc.spi.Statement;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.StatementLogHelper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class DelegateR2DbcParameterHandler implements InvocationHandler {

    private final R2dbcConfiguration configuration;
    private final ParameterHandler parameterHandler;
    private final Map<Class<?>, Field> parameterHandlerFieldMap;
    private final Statement delegateStatement;
    private final PreparedStatement delegatePreparedStatement;
    private final AtomicReference<ParameterHandlerContext> parameterHandlerContextReference = new AtomicReference<>();
    private final StatementLogHelper statementLogHelper;

    public DelegateR2DbcParameterHandler(R2dbcConfiguration r2dbcConfiguration,
                                         ParameterHandler parameterHandler,
                                         Statement statement,
                                         StatementLogHelper statementLogHelper) {
        this.configuration = r2dbcConfiguration;
        this.parameterHandler = parameterHandler;
        this.delegateStatement = statement;
        this.statementLogHelper = statementLogHelper;
        this.delegatePreparedStatement = (PreparedStatement) Proxy.newProxyInstance(
                DelegateR2DbcParameterHandler.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                new DelegateR2dbcStatement(this.delegateStatement,this.configuration.getJdbcParameterAdapterRegistry().getAllJdbcParameterAdapters())
        );
        parameterHandlerFieldMap = Stream.of(parameterHandler.getClass().getDeclaredFields())
                .collect(Collectors.toMap(
                        Field::getType,
                        field -> {
                            field.setAccessible(true);
                            return field;
                        }
                ));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if(!Objects.equals("setParameters",methodName)){
            return method.invoke(parameterHandler,args);
        }
        this.setParameters(this.delegatePreparedStatement);
        return null;
    }

    /**
     * get field
     * @param parameterHandler
     * @param fieldType
     * @param <T>
     * @return
     */
    private <T> T getField(ParameterHandler parameterHandler,Class<T> fieldType){
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
     * @param ps
     */
    public void setParameters(PreparedStatement ps) {
        BoundSql boundSql = this.getField(this.parameterHandler,BoundSql.class);
        TypeHandlerRegistry typeHandlerRegistry = this.getField(this.parameterHandler,TypeHandlerRegistry.class);
        Object parameterObject = parameterHandler.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        ParameterHandlerContext parameterHandlerContext = new ParameterHandlerContext();
        DelegateR2DbcParameterHandler.this.parameterHandlerContextReference.getAndSet(parameterHandlerContext);
        List<Object> columnValues = new ArrayList<>();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
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
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = configuration.getJdbcTypeForNull();
                    }
                    try {
                        if(value == null && jdbcType != null){
                            this.delegateStatement.bindNull(i,parameterMapping.getJavaType());
                            columnValues.add(null);
                        }else {
                            parameterHandlerContext.setIndex(i);
                            parameterHandlerContext.setJavaType(parameterMapping.getJavaType());
                            parameterHandlerContext.setJdbcType(jdbcType);
                            typeHandler.setParameter(ps, i, value, jdbcType);
                            columnValues.add(value);
                        }
                    } catch (TypeException | SQLException e) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                    }
                }
            }
        }
        statementLogHelper.logParameters(columnValues);
    }



    /**
     * delegate Prepare statement
     */
    private class DelegateR2dbcStatement implements InvocationHandler {

        private final Statement statement;
        private final Map<Class, JdbcParameterAdapter> jdbcParameterAdapterContainer;
        private final Set<Class> notSupportedJdbcParameterTypes = new HashSet<>();

        DelegateR2dbcStatement(Statement statement, Map<Class, JdbcParameterAdapter> jdbcParameterAdapterContainer) {
            this.statement = statement;
            this.jdbcParameterAdapterContainer = jdbcParameterAdapterContainer;
            this.loadNotSupportedJdbcParameterTypes();
        }

        private void loadNotSupportedJdbcParameterTypes(){
            notSupportedJdbcParameterTypes.add(InputStream.class);
            notSupportedJdbcParameterTypes.add(SQLXML.class);
            notSupportedJdbcParameterTypes.add(Reader.class);
            notSupportedJdbcParameterTypes.add(StringReader.class);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if(!methodName.startsWith("set")){
                //not handle no set method
                return null;
            }
            int index = (int) args[0];
            Object parameter = args[1];
            Class<?> parameterClass = parameter.getClass();
            //not supported types
            if(notSupportedJdbcParameterTypes.contains(parameterClass)){
                throw new IllegalArgumentException("Unsupported Parameter type : " + parameterClass);
            }
            // using adapter
            if(jdbcParameterAdapterContainer.containsKey(parameterClass)){
                JdbcParameterAdapter jdbcParameterAdapter = jdbcParameterAdapterContainer.get(parameterClass);
                ParameterHandlerContext parameterHandlerContext = DelegateR2DbcParameterHandler.this.parameterHandlerContextReference.get();
                jdbcParameterAdapter.adapt(statement,parameterHandlerContext,parameter);
                return null;
            }
            //default set
            statement.bind(index, parameter);
            return null;
        }


    }

}

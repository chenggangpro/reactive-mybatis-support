package pro.chenggang.project.reactive.mybatis.support.r2dbc.core.impl;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.r2dbc.connectionfactory.ConnectionFactoryUtils;
import org.springframework.data.r2dbc.connectionfactory.ConnectionProxy;
import org.springframework.lang.Nullable;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.binding.MapperProxyFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.type.R2DBCTypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.type.R2dbcTypeHandlerRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.R2dbcMybatisConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * reactive sql session default implementation
 *
 * @author linux_china
 * @author evans
 * note : handle connection close copy from {@link org.springframework.data.r2dbc.core.DefaultDatabaseClient}
 */
@SuppressWarnings("unchecked")
public class DefaultReactiveSqlSession implements ReactiveSqlSession {

    private static final Logger log = LoggerFactory.getLogger(DefaultReactiveSqlSession.class);
    private static final List<Class<?>> NUMBER_TYPES = Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);
    private final R2dbcMybatisConfiguration configuration;
    private final ObjectFactory objectFactory;
    private final ConnectionFactory connectionFactory;
    private final boolean metricsEnabled;

    public DefaultReactiveSqlSession(R2dbcMybatisConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.objectFactory = this.configuration.getObjectFactory();
        //noinspection
        this.connectionFactory = connectionFactory;
        //metrics enabled
        this.metricsEnabled = configuration.isEnableMetrics();
    }

    @Override
    public <T> Mono<T> selectOne(String statementId, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Log statementLog = mappedStatement.getStatementLog();
        Mono<T> rowSelected = this.inConnection(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(statementLog,boundSql);
            List<Object> fillParams = this.fillParameters(statement, boundSql, parameter);
            this.logParameter(statementLog,fillParams);
            ResultMap resultMap = mappedStatement.getResultMaps().get(0);
            LongAdder counter = new LongAdder();
            return Flux.from(statement.execute())
                    .checkpoint("SQL \"" + boundSql.getSql() + "\" [DefaultReactiveSqlSession]")
                    .flatMap(result -> result.map((row, rowMetadata) -> {
                        counter.increment();
                        return (T) this.convertRowToResult(row, rowMetadata, resultMap);
                    }))
                    .singleOrEmpty()
                    .doOnSuccess(result -> this.logResultCount(statementLog,counter.longValue(),false))
                    .onErrorMap(IndexOutOfBoundsException.class,e -> new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found : " + counter.longValue()));
        });
        if (metricsEnabled) {
            return rowSelected.name(statementId).metrics();
        } else {
            return rowSelected;
        }
    }

    @Override
    public <T> Flux<T> select(String statementId, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Log statementLog = mappedStatement.getStatementLog();
        Flux<T> rowsSelected = this.inConnectionMany(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(statementLog,boundSql);
            List<Object> fillParams = this.fillParameters(statement, boundSql, parameter);
            this.logParameter(statementLog,fillParams);
            ResultMap resultMap = mappedStatement.getResultMaps().get(0);
            LongAdder counter = new LongAdder();
            return Flux.from(statement.execute())
                    .checkpoint("SQL \"" + boundSql.getSql() + "\" [DefaultReactiveSqlSession]")
                    .flatMap(result -> result.map((row, rowMetadata) -> {
                        counter.increment();
                        return (T) this.convertRowToResult(row, rowMetadata, resultMap);
                    }))
                    .collectList()
                    .flatMapMany(listResult -> {
                        this.logResultCount(statementLog,counter.longValue(),false);
                        return Flux.fromIterable(listResult);
                    });
        });
        if (metricsEnabled) {
            return rowsSelected.name(statementId).metrics();
        } else {
            return rowsSelected;
        }
    }

    @Override
    public <T> Flux<T> select(String statementId, Object parameter, RowBounds rowBounds) {
        return (Flux<T>) this.select(statementId, parameter)
                .skip(rowBounds.getOffset())
                .limitRequest(rowBounds.getLimit());
    }

    @Override
    public Mono<Integer> insert(String statementId, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Log statementLog = mappedStatement.getStatementLog();
        Mono<Integer> rowsUpdated = this.inConnection(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(statementLog,boundSql);
            final boolean useGeneratedKeys = mappedStatement.getKeyGenerator() != null && mappedStatement.getKeyProperties() != null;
            if (useGeneratedKeys) {
                statement.returnGeneratedValues(mappedStatement.getKeyProperties());
            }
            List<Object> fillParams = this.fillParameters(statement, boundSql, parameter);
            this.logParameter(statementLog,fillParams);
            return Mono.from(statement.execute())
                    .checkpoint("SQL \"" + boundSql.getSql() + "\" [DefaultReactiveSqlSession]")
                    .flatMap(result -> {
                        if (!useGeneratedKeys) {
                            return Mono.from(result.getRowsUpdated());
                        } else {
                            return Flux.from(result.map((row, rowMetadata) -> {
                                MetaObject parameterMetaObject = configuration.newMetaObject(parameter);
                                for (String keyProperty : mappedStatement.getKeyProperties()) {
                                    Object value = row.get(keyProperty, parameterMetaObject.getSetterType(keyProperty));
                                    parameterMetaObject.setValue(keyProperty, value);
                                }
                                return 1;
                            })).reduce(Integer::sum);
                        }
                    })
                    .doOnSuccess(result -> this.logResultCount(statementLog,result,true));
        });
        if (metricsEnabled) {
            return rowsUpdated.name(statementId).metrics();
        } else {
            return rowsUpdated;
        }
    }

    @Override
    public Mono<Integer> delete(String statementId, Object parameter) {
        return this.update(statementId, parameter);
    }

    @Override
    public Mono<Integer> update(String statementId, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Log statementLog = mappedStatement.getStatementLog();
        Mono<Integer> updatedRows = this.inConnection(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(statementLog,boundSql);
            List<Object> fillParams = this.fillParameters(statement, boundSql, parameter);
            this.logParameter(statementLog,fillParams);
            return Mono.from(statement.execute())
                    .checkpoint("SQL \"" + boundSql.getSql() + "\" [DefaultReactiveSqlSession]")
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .doOnSuccess(result -> this.logResultCount(statementLog,result,true));
        });
        if (metricsEnabled) {
            return updatedRows.name(statementId).metrics();
        } else {
            return updatedRows;
        }
    }

    @Override
    public <T> T getMapper(Class<T> clazz) {
        MapperProxyFactory<T> mapperProxyFactory = new MapperProxyFactory<>(clazz);
        return mapperProxyFactory.newInstance(this);
    }


    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * fill params
     * @param statement
     * @param boundSql
     * @param parameter
     * @return
     */
    public List<Object> fillParameters(Statement statement, BoundSql boundSql, Object parameter) {
        if(parameter == null){
            return Collections.emptyList();
        }
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        List<Object> parameterValues = new ArrayList<>();
        if (parameterMappings != null) {
            R2dbcTypeHandlerRegistry r2dbcTypeHandlerRegistry = this.configuration.getR2dbcTypeHandlerRegistry();
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameter == null) {
                        value = null;
                    } else if (r2dbcTypeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
                        value = parameter;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(parameter);
                        value = metaObject.getValue(propertyName);
                    }
                    TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
                    try {
                        if (typeHandler instanceof R2DBCTypeHandler) {
                            ((R2DBCTypeHandler<Object>) typeHandler).setParameter(statement, i, value, parameterMapping.getJdbcType());
                        } else {
                            if (value == null) {
                                statement.bindNull(i, parameterMapping.getJavaType());
                            } else {
                                Class<?> parameterClass = value.getClass();
                                if (r2dbcTypeHandlerRegistry.hasTypeHandler(parameterClass)) {
                                    r2dbcTypeHandlerRegistry.getTypeHandler(parameterClass).setParameter(statement, i, value, parameterMapping.getJdbcType());
                                } else {
                                    statement.bind(i, value);
                                }
                            }
                        }
                    } catch (TypeException e) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                    }
                    parameterValues.add(value);
                }
            }
        }
        return parameterValues;
    }

    public Object convertRowToResult(Row row, RowMetadata rowMetadata, ResultMap resultMap) {
        //number
        Class<?> type = resultMap.getType();
        R2dbcTypeHandlerRegistry r2dbcTypeHandlerRegistry = this.configuration.getR2dbcTypeHandlerRegistry();
        if (NUMBER_TYPES.contains(type)) {
            Number columnValue = (Number) row.get(0);
            if (columnValue == null) {
                return null;
            }
            if (type.equals(columnValue.getClass())) {
                return columnValue;
            } else if (type.equals(Byte.class) || type.equals(byte.class)) {
                return columnValue.byteValue();
            } else if (type.equals(Short.class) || type.equals(short.class)) {
                return columnValue.shortValue();
            } else if (type.equals(Integer.class) || type.equals(int.class)) {
                return columnValue.intValue();
            } else if (type.equals(Long.class) || type.equals(long.class)) {
                return columnValue.longValue();
            } else if (type.equals(Float.class) || type.equals(float.class)) {
                return columnValue.floatValue();
            } else if (type.equals(Double.class) || type.equals(double.class)) {
                return columnValue.doubleValue();
            } else {
                return columnValue;
            }
        } else if (r2dbcTypeHandlerRegistry.hasTypeHandler(type)) {
            R2DBCTypeHandler<?> mappingTypeHandler = r2dbcTypeHandlerRegistry.getTypeHandler(type);
            return mappingTypeHandler.getResult(row, 0, rowMetadata);
        } else if (!resultMap.getResultMappings().isEmpty()) {
            Object object = objectFactory.create(type);
            MetaObject resultMetaObject = configuration.newMetaObject(object);
            List<ResultMapping> resultMappings = resultMap.getResultMappings();
            if (!resultMappings.isEmpty()) {
                for (ResultMapping resultMapping : resultMappings) {
                    Class<?> javaType = resultMapping.getJavaType();
                    Object columnValue;
                    TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
                    if (typeHandler instanceof R2DBCTypeHandler) {
                        columnValue = ((R2DBCTypeHandler<?>) typeHandler).getResult(row, resultMapping.getColumn(), rowMetadata);
                    } else if (r2dbcTypeHandlerRegistry.hasTypeHandler(javaType)) {
                        columnValue = r2dbcTypeHandlerRegistry.getTypeHandler(javaType).getResult(row, resultMapping.getColumn(), rowMetadata);
                    } else {
                        columnValue = row.get(resultMapping.getColumn(), javaType);
                    }
                    resultMetaObject.setValue(resultMapping.getProperty(), columnValue);
                }
            } else {
                rowMetadata.getColumnNames().forEach(column -> {
                    Object columnValue = row.get(column);
                    final String property = resultMetaObject.findProperty(column, configuration.isMapUnderscoreToCamelCase());
                    resultMetaObject.setValue(property, columnValue);
                });
            }
            return object;
        } else if (type.isAssignableFrom(Map.class)) {
            Map<String, Object> result = new HashMap<>();
            for (String columnName : rowMetadata.getColumnNames()) {
                result.put(columnName, row.get(columnName));
            }
            return result;
        } else if (type.isAssignableFrom(Collection.class)) {
            List<Object> result = new ArrayList<>();
            for (String columnName : rowMetadata.getColumnNames()) {
                result.add(row.get(columnName));
            }
            return result;
        } else {
            return row.get(0, type);
        }
    }

    /**
     * log statement
     * @param statementLog
     * @param boundSql
     */
    private void logStatement(Log statementLog ,BoundSql boundSql){
        if(statementLog.isDebugEnabled()){
            statementLog.debug("==>  Preparing: " + boundSql.getSql().replaceAll("\\s+", " "));
        }
    }

    /**
     * log params
     * @param statementLog
     * @param parameterValues
     */
    private void logParameter(Log statementLog ,List<Object> parameterValues){
        if(statementLog.isDebugEnabled()){
            statementLog.debug("==>  Parameters:" + parameterValues.stream().map(String::valueOf).collect(Collectors.joining(" , ")));
        }
    }

    /**
     * log result count
     * @param statementLog
     * @param resultCount
     * @param isUpdate
     */
    private void logResultCount(Log statementLog,long resultCount,boolean isUpdate){
        if(statementLog.isDebugEnabled()){
            statementLog.debug("<==  " + ( isUpdate ? "Updates" : "Total" )+ ": " + resultCount);
        }
    }

    /**
     * Execute a callback {@link Function} within a {@link Connection} scope. The function is responsible for creating a
     * {@link Mono}. The connection is released after the {@link Mono} terminates (or the subscription is cancelled).
     * Connection resources must not be passed outside of the {@link Function} closure, otherwise resources may get
     * defunct.
     *
     * @param action must not be {@literal null}.
     * @return the resulting {@link Mono}.
     * @throws DataAccessException when during construction of the {@link Mono} a problem occurs.
     */
    public <T> Mono<T> inConnection(Function<Connection, Mono<T>> action) throws DataAccessException {
        Mono<ConnectionCloseHolder> connectionMono = Mono.from(this.connectionFactory.create())
                .doOnNext(connection -> log.debug("Execute Statement With Mono,Get Connection [{}] From Connection Factory ",connection))
                .map(it -> new ConnectionCloseHolder(it, this::closeConnection));
        return Mono.usingWhen(connectionMono,
                it -> {
                    // Create close-suppressing Connection proxy
                    Connection connectionToUse = this.createConnectionProxy(it.connection);
                    return action.apply(connectionToUse);
                },
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    /**
     * Execute a callback {@link Function} within a {@link Connection} scope. The function is responsible for creating a
     * {@link Flux}. The connection is released after the {@link Flux} terminates (or the subscription is cancelled).
     * Connection resources must not be passed outside of the {@link Function} closure, otherwise resources may get
     * defunct.
     *
     * @param action must not be {@literal null}.
     * @return the resulting {@link Flux}.
     * @throws DataAccessException when during construction of the {@link Mono} a problem occurs.
     */
    public <T> Flux<T> inConnectionMany(Function<Connection, Flux<T>> action) throws DataAccessException {
        Mono<ConnectionCloseHolder> connectionMono = Mono.from(this.connectionFactory.create())
                .doOnNext(connection -> log.debug("Execute Statement With Flux,Get Connection [{}] From Connection Factory ",connection))
                .map(it -> new ConnectionCloseHolder(it, this::closeConnection));
        return Flux.usingWhen(connectionMono,
                it -> {
                    // Create close-suppressing Connection proxy, also preparing returned Statements.
                    Connection connectionToUse = this.createConnectionProxy(it.connection);
                    return action.apply(connectionToUse);
                },
                ConnectionCloseHolder::close,
                (it, err) -> it.close(),
                ConnectionCloseHolder::close);
    }

    /**
     * Create a close-suppressing proxy for the given R2DBC Connection. Called by the {@code execute} method.
     *
     * @param con the R2DBC Connection to create a proxy for
     * @return the Connection proxy
     */
    protected Connection createConnectionProxy(Connection con) {
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
                new Class<?>[] { ConnectionProxy.class },
                new CloseSuppressingInvocationHandler(con));
    }

    /**
     * Release the {@link Connection}.
     *
     * @param connection to close.
     * @return a {@link Publisher} that completes successfully when the connection is closed.
     */
    protected Publisher<Void> closeConnection(Connection connection) {
        return ConnectionFactoryUtils.currentConnectionFactory(this.connectionFactory).then()
                .onErrorResume(Exception.class, e -> Mono.from(connection.close()));
    }

    /**
     * Invocation handler that suppresses close calls on R2DBC Connections. Also prepares returned Statement
     * (Prepared/CallbackStatement) objects.
     *
     * @see Connection#close()
     */
    private static class CloseSuppressingInvocationHandler implements InvocationHandler {

        private final Connection target;

        CloseSuppressingInvocationHandler(Connection target) {
            this.target = target;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Invocation on ConnectionProxy interface coming in...

            if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return proxy == args[0];
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of PersistenceManager proxy.
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("unwrap")) {
                return target;
            } else if (method.getName().equals("close")) {
                // Handle close method: suppress, not valid.
                return Mono.error(new UnsupportedOperationException("Close is not supported!"));
            } else if (method.getName().equals("getTargetConnection")) {
                // Handle getTargetConnection method: return underlying Connection.
                return this.target;
            }

            // Invoke method on target Connection.
            try {
                return method.invoke(this.target, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

    /**
     * Holder for a connection that makes sure the close action is invoked atomically only once.
     */
    static class ConnectionCloseHolder extends AtomicBoolean {

        final Connection connection;
        final Function<Connection, Publisher<Void>> closeFunction;

        ConnectionCloseHolder(Connection connection, Function<Connection, Publisher<Void>> closeFunction) {
            this.connection = connection;
            this.closeFunction = closeFunction;
        }

        Mono<Void> close() {
            return Mono.defer(() -> {
                if (compareAndSet(false, true)) {
                    log.debug("Release Connection [{}] ",connection);
                    return Mono.from(this.closeFunction.apply(this.connection));
                }
                return Mono.empty();
            });
        }
    }
}

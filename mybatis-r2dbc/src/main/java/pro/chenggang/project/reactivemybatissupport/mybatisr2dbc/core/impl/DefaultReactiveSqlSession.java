package pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.impl;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
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
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.ReactiveSqlSession;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.binding.MapperProxyFactory;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.type.R2DBCTypeHandler;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.type.R2dbcTypeHandlerRegistry;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.support.R2dbcMybatisConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * reactive sql session default implementation
 *
 * @author linux_china
 */
@Slf4j
@SuppressWarnings("unchecked")
public class DefaultReactiveSqlSession implements ReactiveSqlSession {
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
        Mono<T> rowSelected = this.getConnection().flatMap(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(boundSql);
            if (parameter != null) {
                this.fillParams(statement, boundSql, parameter);
            }else {
                if(log.isDebugEnabled()){
                    log.debug("==>  Parameters:  ");
                }
            }
            ResultMap resultMap = mappedStatement.getResultMaps().get(0);
            AtomicLong counter = new AtomicLong(0L);
            return this.executeFluxStatement(connection, statement)
                    .flatMap(result -> result.map((row, rowMetadata) -> {
                        counter.getAndIncrement();
                        return (T) this.convertRowToResult(row, rowMetadata, resultMap);
                    }))
                    .singleOrEmpty()
                    .doOnSuccess(result -> {
                        if(log.isDebugEnabled()){
                            log.debug("<==  Total: {}",counter.get());
                        }
                    })
                    .onErrorMap(IndexOutOfBoundsException.class,e -> new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found : " + counter.get()));
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
        Flux<T> rowsSelected = this.getConnection().flatMapMany(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(boundSql);
            if (parameter != null) {
                this.fillParams(statement, boundSql, parameter);
            }else {
                if(log.isDebugEnabled()){
                    log.debug("==>  Parameters:  ");
                }
            }
            ResultMap resultMap = mappedStatement.getResultMaps().get(0);
            AtomicLong counter = new AtomicLong(0L);
            return this.executeFluxStatement(connection, statement)
                    .flatMap(result -> result.map((row, rowMetadata) -> {
                        counter.getAndIncrement();
                        return (T) this.convertRowToResult(row, rowMetadata, resultMap);
                    }))
                    .collectList()
                    .flatMapMany(listResult -> {
                        if(log.isDebugEnabled()){
                            log.debug("<==  Total: {}",counter.get());
                        }
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
        Mono<Integer> rowsUpdated = this.getConnection().flatMap(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(boundSql);
            final boolean useGeneratedKeys = mappedStatement.getKeyGenerator() != null && mappedStatement.getKeyProperties() != null;
            if (useGeneratedKeys) {
                statement.returnGeneratedValues(mappedStatement.getKeyProperties());
            }
            if (parameter != null) {
                this.fillParams(statement, boundSql, parameter);
            }else {
                if(log.isDebugEnabled()){
                    log.debug("==>  Parameters:  ");
                }
            }
            return this.executeMonoStatement(connection, statement)
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
                    .doOnNext(resultRowsUpdated -> {
                        if(log.isDebugEnabled()){
                            log.debug("<==  Updates: {}",resultRowsUpdated);
                        }
                    });
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
        Mono<Integer> updatedRows = this.getConnection().flatMap(connection -> {
            Statement statement = connection.createStatement(boundSql.getSql());
            this.logStatement(boundSql);
            if (parameter != null) {
                this.fillParams(statement, boundSql, parameter);
            }else {
                if(log.isDebugEnabled()){
                    log.debug("==>  Parameters:  ");
                }
            }
            return this.executeMonoStatement(connection, statement)
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .doOnNext(rowsUpdated -> {
                        if(log.isDebugEnabled()){
                            log.debug("<==  Updates: {}",rowsUpdated);
                        }
                    });
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

    public void fillParams(Statement statement, BoundSql boundSql, Object parameter) {
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
        if(log.isDebugEnabled()){
            log.debug("==>  Parameters: {}",parameterValues.stream().map(String::valueOf).collect(Collectors.joining(" , ")));
        }
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

    private Flux<? extends Result> executeFluxStatement(Connection connection, Statement statement) {
        return Flux.from(statement.execute())
                .doFinally(a -> ((Mono) connection.close()).subscribe());
    }

    private Mono<? extends Result> executeMonoStatement(Connection connection, Statement statement) {
        return Mono.from(statement.execute())
                .doFinally(a -> ((Mono) connection.close()).subscribe());
    }

    private Mono<Connection> getConnection() {
        Mono<Connection> connection = (Mono<Connection>) connectionFactory.create();
        return connection;
    }

    private void logStatement(BoundSql boundSql){
        if(log.isDebugEnabled()){
            log.debug("==>  Preparing: {}",boundSql.getSql().replaceAll("\\s+", " "));
        }
    }

}

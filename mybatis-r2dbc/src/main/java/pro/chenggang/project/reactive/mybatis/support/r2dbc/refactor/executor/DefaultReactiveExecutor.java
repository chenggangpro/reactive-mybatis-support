package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor;

import io.r2dbc.spi.*;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.DelegateR2DbcParameterHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.type.R2DBCTypeHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.type.R2dbcTypeHandlerRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.session.defaults.DefaultReactiveSqlSession.NUMBER_TYPES;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class DefaultReactiveExecutor extends AbstractReactiveExecutor{

    private static final Log log = LogFactory.getLog(DefaultReactiveExecutor.class);

    public DefaultReactiveExecutor(R2dbcConfiguration configuration, ConnectionFactory connectionFactory) {
        super(configuration, connectionFactory);
    }

    @Override
    protected Flux<? extends Result> doUpdateWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter) {
        return Flux.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext::getStatementLogHelper)
                .flatMapMany(statementLogHelper -> {
                    String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                    Statement statement = createStatementInternal(connection,boundSql, mappedStatement, parameter, RowBounds.DEFAULT,statementLogHelper);
                    return Flux.from(statement.execute())
                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]");
                })
        );
    }

    @Override
    protected <E> Flux<E> doQueryWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds) {
        return Flux.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext::getStatementLogHelper)
                .flatMapMany(statementLogHelper -> {
                    String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                    Statement statement = this.createStatementInternal(connection,boundSql, mappedStatement, parameter, rowBounds,statementLogHelper);
                    LongAdder totalCounter = new LongAdder();
                    return Flux.from(statement.execute())
                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                            .flatMap(result -> result
                                    .map((row, rowMetadata) -> {
                                        ResultMap resultMap = mappedStatement.getResultMaps().get(0);
                                        //TODO result Handler
                                        E e = (E) this.convertRowToResult(row, rowMetadata, resultMap);
                                        totalCounter.increment();
                                        return e;
                                    })
                            )
                            .doOnComplete(() -> statementLogHelper.logTotal(totalCounter.intValue()));
                })
        );



    }

    /**
     * create statement
     * @param connection
     * @param mappedStatement
     * @param parameter
     * @param rowBounds
     * @return
     */
    private Statement createStatementInternal(Connection connection,
                                              String boundSql,
                                              MappedStatement mappedStatement,
                                              Object parameter,
                                              RowBounds rowBounds,
                                              StatementLogHelper statementLogHelper){
        statementLogHelper.logSql(boundSql);
        StatementHandler handler = configuration.newStatementHandler(null, mappedStatement, parameter, rowBounds, null, null);
        ParameterHandler parameterHandler = handler.getParameterHandler();
        Statement statement = connection.createStatement(boundSql);
        ParameterHandler delegateParameterHandler = (ParameterHandler) Proxy.newProxyInstance(DefaultReactiveExecutor.class.getClassLoader(),
                new Class[]{ParameterHandler.class},
                new DelegateR2DbcParameterHandler(this.configuration, parameterHandler, statement, statementLogHelper)
        );
        try {
            delegateParameterHandler.setParameters(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statement;
    }


    public Object convertRowToResult(Row row, RowMetadata rowMetadata, ResultMap resultMap) {
        //number
        Class<?> type = resultMap.getType();
        R2dbcTypeHandlerRegistry r2dbcTypeHandlerRegistry = null;
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
        } else if (Objects.nonNull(r2dbcTypeHandlerRegistry) && r2dbcTypeHandlerRegistry.hasTypeHandler(type)) {
            R2DBCTypeHandler<?> mappingTypeHandler = r2dbcTypeHandlerRegistry.getTypeHandler(type);
            return mappingTypeHandler.getResult(row, 0, rowMetadata);
        } else if (!resultMap.getResultMappings().isEmpty()) {
            Object object = this.configuration.getObjectFactory().create(type);
            MetaObject resultMetaObject = configuration.newMetaObject(object);
            List<ResultMapping> resultMappings = resultMap.getResultMappings();
            if (!resultMappings.isEmpty()) {
                for (ResultMapping resultMapping : resultMappings) {
                    Class<?> javaType = resultMapping.getJavaType();
                    Object columnValue;
                    TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
                    if (typeHandler instanceof R2DBCTypeHandler) {
                        columnValue = ((R2DBCTypeHandler<?>) typeHandler).getResult(row, resultMapping.getColumn(), rowMetadata);
                    } else if (Objects.nonNull(r2dbcTypeHandlerRegistry) && r2dbcTypeHandlerRegistry.hasTypeHandler(javaType)) {
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
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.exception.R2dbcParameterException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.DelegateR2dbcParameterHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.DefaultReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.ReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.support.ProxyInstanceFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Collectors;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.ReactiveResultHandler.DEFERRED;

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
    protected Mono<Integer> doUpdateWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter) {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext::getStatementLogHelper)
                .flatMap(statementLogHelper -> {
                    String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                    Statement statement = createStatementInternal(connection,boundSql, mappedStatement, parameter, RowBounds.DEFAULT,statementLogHelper);
                    final boolean useGeneratedKeys = mappedStatement.getKeyGenerator() != null && mappedStatement.getKeyProperties() != null;
//                    R2dbcKeyGenerator r2dbcKeyGenerator = new DefaultR2dbcKeyGenerator();
                    return Flux.from(statement.execute())
                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                            .flatMap(result -> {
                                if (!useGeneratedKeys) {
                                    return Mono.from(result.getRowsUpdated());
                                } else {
                                    //TODO using R2dbc key generator
//                                    Flux.just(result)
//                                            .flatMap(targetResult -> targetResult.map((row,rowMetadata) -> new RowResultWrapper(row,rowMetadata,configuration)))
//                                            .flatMap(rowResultWrapper -> r2dbcKeyGenerator.handleKeyResult(rowResultWrapper)
//                                                    .then(Mono.just(1))
//                                            );
                                    return Flux.from(result.map((row, rowMetadata) -> {
                                        MetaObject parameterMetaObject = configuration.newMetaObject(parameter);
                                        for (String keyProperty : mappedStatement.getKeyProperties()) {
                                            Object value = row.get(keyProperty, parameterMetaObject.getSetterType(keyProperty));
                                            parameterMetaObject.setValue(keyProperty, value);
                                        }
                                        return 1;
                                    }));
                                }
                            })
                            .collect(Collectors.summingInt(Integer::intValue))
                            .doOnNext(statementLogHelper::logUpdates);
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
                    ReactiveResultHandler reactiveResultHandler = new DefaultReactiveResultHandler(configuration, mappedStatement);
                    return Flux.from(statement.execute())
                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                            .skip(rowBounds.getOffset())
                            .takeWhile(result -> reactiveResultHandler.getResultRowTotalCount() < rowBounds.getLimit())
                            .flatMap(result -> result.map((row,rowMetadata) -> new RowResultWrapper(row,rowMetadata,configuration)))
                            .flatMap(rowResultWrapper -> (Publisher<E>) reactiveResultHandler.handleResult(rowResultWrapper))
                            .filter(data -> !Objects.equals(data,DEFERRED))
                            .doOnComplete(() -> statementLogHelper.logTotal(reactiveResultHandler.getResultRowTotalCount()));
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
        //only support generated keys by  key properties
        //not support generated keys by select key
        final boolean useGeneratedKeys = mappedStatement.getKeyGenerator() != null && mappedStatement.getKeyProperties() != null;
        if (useGeneratedKeys) {
            statement.returnGeneratedValues(mappedStatement.getKeyProperties());
        }
        ParameterHandler delegateParameterHandler = ProxyInstanceFactory.newInstanceOfInterfaces(
                ParameterHandler.class,
                () -> new DelegateR2dbcParameterHandler(
                        this.configuration,
                        parameterHandler,
                        statement,
                        statementLogHelper)
        );
        try {
            delegateParameterHandler.setParameters(null);
        } catch (SQLException e) {
            throw new R2dbcParameterException(e);
        }
        return statement;
    }

}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextHelper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.exception.R2dbcParameterException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.DefaultR2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.DelegateR2dbcParameterHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.DefaultReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.ReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.ReactiveResultHandler.DEFERRED;

/**
 * The type Default reactive mybatis executor.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12/9/21.
 */
public class DefaultReactiveMybatisExecutor extends AbstractReactiveMybatisExecutor {

    private static final Log log = LogFactory.getLog(DefaultReactiveMybatisExecutor.class);

    /**
     * Instantiates a new Default reactive mybatis executor.
     *
     * @param configuration the configuration
     */
    public DefaultReactiveMybatisExecutor(R2dbcMybatisConfiguration configuration) {
        super(configuration, configuration.getConnectionFactory());
    }

    @Override
    protected Mono<Integer> doUpdateWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter) {
        return MybatisReactiveContextHelper.currentContext()
                .map(ReactiveExecutorContext::getStatementLogHelper)
                .flatMap(statementLogHelper -> {
                    String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                    Statement statement = this.createStatementInternal(connection, boundSql, mappedStatement, parameter, RowBounds.DEFAULT, statementLogHelper);
                    return Mono.just(this.isUseGeneratedKeys(mappedStatement))
                            .filter(useGeneratedKeys -> useGeneratedKeys)
                            .map(useGeneratedKeys -> new DefaultR2dbcKeyGenerator(mappedStatement, super.configuration))
                            .flatMapMany(r2dbcKeyGenerator -> Flux.from(statement.execute())
                                    .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                                    .flatMap(result -> {
                                        int keyPropertiesLength = mappedStatement.getKeyProperties().length;
                                        return Flux.just(result)
                                                .take(keyPropertiesLength, true)
                                                .flatMap(targetResult -> targetResult.map((row, rowMetadata) -> {
                                                    RowResultWrapper rowResultWrapper = new RowResultWrapper(row, rowMetadata, configuration);
                                                    return r2dbcKeyGenerator.handleKeyResult(rowResultWrapper, parameter);
                                                }));
                                    })
                            )
                            .switchIfEmpty(Flux
                                    .defer(() -> Flux
                                            .from(statement.execute())
                                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                                            .flatMap(result -> Mono.from(result.getRowsUpdated()))
                                    )
                            )
                            .collect(Collectors.summingInt(Integer::intValue))
                            .doOnNext(statementLogHelper::logUpdates);
                });
    }

    @Override
    protected <E> Flux<E> doQueryWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds) {
        return MybatisReactiveContextHelper.currentContext()
                .map(ReactiveExecutorContext::getStatementLogHelper)
                .flatMapMany(statementLogHelper -> {
                    String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                    Statement statement = this.createStatementInternal(connection, boundSql, mappedStatement, parameter, rowBounds, statementLogHelper);
                    ReactiveResultHandler reactiveResultHandler = new DefaultReactiveResultHandler(configuration, mappedStatement);
                    return Flux.from(statement.execute())
                            .checkpoint("SQL: \"" + boundSql + "\" [DefaultReactiveExecutor]")
                            .skip(rowBounds.getOffset())
                            .take(rowBounds.getLimit(), true)
                            .concatMap(result -> result.map((row, rowMetadata) -> {
                                RowResultWrapper rowResultWrapper = new RowResultWrapper(row, rowMetadata, configuration);
                                return (List<E>) reactiveResultHandler.handleResult(rowResultWrapper);
                            }))
                            .concatMap(Flux::fromIterable)
                            .filter(data -> !Objects.equals(data, DEFERRED))
                            .doOnComplete(() -> statementLogHelper.logTotal(reactiveResultHandler.getResultRowTotalCount()));
                });

    }

    /**
     * create statement internal
     *
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
                                              StatementLogHelper statementLogHelper) {
        statementLogHelper.logSql(boundSql);
        StatementHandler handler = configuration.newStatementHandler(null, mappedStatement, parameter, rowBounds, null, null);
        ParameterHandler parameterHandler = handler.getParameterHandler();
        Statement statement = connection.createStatement(boundSql);
        //only support generated keys by key properties
        //not support generated keys by select key
        final boolean useGeneratedKeys = this.isUseGeneratedKeys(mappedStatement);
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

    /**
     * is use generated keys or not
     *
     * @param mappedStatement MappedStatement
     * @return true when using generated keys
     */
    private boolean isUseGeneratedKeys(MappedStatement mappedStatement) {
        boolean hasKeyProperties = mappedStatement.getKeyProperties() != null && mappedStatement.getKeyProperties().length != 0;
        return mappedStatement.getKeyGenerator() != null && hasKeyProperties;
    }

}
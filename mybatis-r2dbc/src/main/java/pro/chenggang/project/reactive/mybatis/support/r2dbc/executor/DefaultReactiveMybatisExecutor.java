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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.exception.GeneratedKeysException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.exception.R2dbcParameterException;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.DefaultR2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.NoKeyR2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.R2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.SelectR2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.DelegateR2dbcParameterHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderFormatter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults.DefaultPlaceholderFormatter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.DefaultReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.ReactiveResultHandler;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.KeyGeneratorType.SELECT_KEY_AFTER;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.KeyGeneratorType.SELECT_KEY_BEFORE;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.KeyGeneratorType.SIMPLE_RETURN;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.ReactiveResultHandler.DEFERRED;

/**
 * The type Default reactive mybatis executor.
 *
 * @author Gang Cheng
 * @version 1.0.2
 * @since 1.0.0
 */
public class DefaultReactiveMybatisExecutor extends AbstractReactiveMybatisExecutor {

    private static final Log log = LogFactory.getLog(DefaultReactiveMybatisExecutor.class);

    /**
     * The Placeholder formatter.
     */
    protected PlaceholderFormatter placeholderFormatter;

    /**
     * Instantiates a new Default reactive mybatis executor.
     *
     * @param configuration the configuration
     */
    public DefaultReactiveMybatisExecutor(R2dbcMybatisConfiguration configuration) {
        super(configuration, configuration.getR2dbcEnvironment().getConnectionFactory());
        this.placeholderFormatter = new DefaultPlaceholderFormatter(
                configuration.getPlaceholderDialectRegistry(),
                configuration.getFormattedDialectSqlCacheMaxSize(),
                configuration.getFormattedDialectSqlCacheExpireDuration()
        );
    }

    @Override
    protected Mono<Integer> doUpdateWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter) {
        return MybatisReactiveContextManager.currentContext()
                .doOnNext(reactiveExecutorContext -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Do update with connection from context : " + reactiveExecutorContext);
                    }
                })
                .map(ReactiveExecutorContext::getR2dbcStatementLog)
                .flatMap(r2dbcStatementLog -> {
                    R2dbcKeyGenerator r2dbcKeyGenerator = this.getR2dbcKeyGenerator(mappedStatement);
                    return MybatisReactiveContextManager.currentContextAttribute()
                            .flatMap(attribute -> r2dbcKeyGenerator.processSelectKey(SELECT_KEY_BEFORE, mappedStatement, parameter)
                                    .flatMapMany(ignoreResult -> {
                                        String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                                        boolean isSimpleReturnedGeneratedKeys = SIMPLE_RETURN.equals(r2dbcKeyGenerator.keyGeneratorType());
                                        Statement statement = this.createStatementInternal(connection, boundSql, mappedStatement, parameter, RowBounds.DEFAULT, isSimpleReturnedGeneratedKeys, attribute, r2dbcStatementLog);
                                        if(isSimpleReturnedGeneratedKeys){
                                            return Flux.from(statement
                                                                  .fetchSize(mappedStatement.getKeyProperties().length)
                                                                  .execute()
                                                    )
                                                    .checkpoint("[DefaultReactiveExecutor] SQL: \"" + boundSql + "\" ")
                                                    .concatMap(result -> result.map((row, rowMetadata) -> {
                                                        RowResultWrapper rowResultWrapper = new RowResultWrapper(row, rowMetadata, configuration);
                                                        return r2dbcKeyGenerator.processGeneratedKeyResult(rowResultWrapper, parameter);
                                                    }));
                                        }
                                        return Flux.from(statement.execute())
                                                .checkpoint("[DefaultReactiveExecutor]SQL: \"" + boundSql + "\" ")
                                                .concatMap(result -> Mono.from(result.getRowsUpdated()));
                                    })
                                    .collect(Collectors.summingInt(Integer::intValue))
                                    .defaultIfEmpty(0)
                                    .doOnNext(r2dbcStatementLog::logUpdates)
                                    .flatMap(totalUpdateRowCount -> r2dbcKeyGenerator.processSelectKey(SELECT_KEY_AFTER, mappedStatement, parameter)
                                            .flatMap(ignore -> Mono.just(totalUpdateRowCount))
                                    )
                            );
                });
    }

    @Override
    protected <E> Flux<E> doQueryWithConnection(Connection connection, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds) {
        return MybatisReactiveContextManager.currentContext()
                .doOnNext(reactiveExecutorContext -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Do query with connection from context : " + reactiveExecutorContext);
                    }
                })
                .map(ReactiveExecutorContext::getR2dbcStatementLog)
                .flatMapMany(r2dbcStatementLog -> MybatisReactiveContextManager.currentContextAttribute()
                        .flatMapMany(attribute -> {
                            String boundSql = mappedStatement.getBoundSql(parameter).getSql();
                            Statement statement = this.createStatementInternal(connection, boundSql, mappedStatement, parameter, rowBounds, false, attribute, r2dbcStatementLog);
                            ReactiveResultHandler reactiveResultHandler = new DefaultReactiveResultHandler(configuration, mappedStatement);
                            if(RowBounds.NO_ROW_OFFSET == rowBounds.getOffset()){
                                statement.fetchSize(rowBounds.getLimit());
                            }
                            return Flux.from(statement.execute())
                                    .checkpoint("[DefaultReactiveExecutor] SQL: \"" + boundSql + "\"")
                                    .skip(rowBounds.getOffset())
                                    .take(rowBounds.getLimit(), true)
                                    .concatMap(result -> result.map((row, rowMetadata) -> {
                                        RowResultWrapper rowResultWrapper = new RowResultWrapper(row, rowMetadata, configuration);
                                        return (E) reactiveResultHandler.handleResult(rowResultWrapper);
                                    }))
                                    .concatWith(Flux.defer(() -> Flux
                                            .fromIterable((Collection<E>)reactiveResultHandler.getRemainedResults())
                                            .filter(Objects::nonNull))
                                    )
                                    .filter(data -> !Objects.equals(data, DEFERRED))
                                    .doOnCancel(() -> {
                                        //clean up reactiveResultHandler
                                        reactiveResultHandler.cleanup();
                                        r2dbcStatementLog.logTotal(reactiveResultHandler.getResultRowTotalCount());
                                    })
                                    .doOnComplete(() -> {
                                        //clean up reactiveResultHandler
                                        reactiveResultHandler.cleanup();
                                        r2dbcStatementLog.logTotal(reactiveResultHandler.getResultRowTotalCount());
                                    });
                        }));

    }

    /**
     * create statement internal
     *
     * @param connection      the connection
     * @param mappedStatement the mappedStatement
     * @param parameter       the parameter
     * @param rowBounds       the rowBounds
     * @return R2dbc's Statement
     */
    private Statement createStatementInternal(Connection connection,
                                              String boundSql,
                                              MappedStatement mappedStatement,
                                              Object parameter,
                                              RowBounds rowBounds,
                                              boolean returnedGeneratedKeys,
                                              ReactiveExecutorContextAttribute reactiveExecutorContextAttribute,
                                              R2dbcStatementLog r2dbcStatementLog) {
        r2dbcStatementLog.logSql(boundSql);
        StatementHandler handler = configuration.newStatementHandler(null, mappedStatement, parameter, rowBounds, null, null);
        ParameterHandler parameterHandler = handler.getParameterHandler();
        BoundSql originalBoundSql = mappedStatement.getBoundSql(parameter);
        String formattedSql = this.placeholderFormatter.replaceSqlPlaceholder(connection.getMetadata(), originalBoundSql, reactiveExecutorContextAttribute);
        Statement statement = connection.createStatement(formattedSql);
        if (returnedGeneratedKeys) {
            statement.returnGeneratedValues(mappedStatement.getKeyColumns());
        }
        ParameterHandler delegateParameterHandler = ProxyInstanceFactory.newInstanceOfInterfaces(
                ParameterHandler.class,
                () -> new DelegateR2dbcParameterHandler(
                        this.configuration,
                        parameterHandler,
                        statement,
                        r2dbcStatementLog)
        );
        try {
            delegateParameterHandler.setParameters(null);
        } catch (SQLException e) {
            throw new R2dbcParameterException(e);
        }
        if(Objects.nonNull(rowBounds)){
            statement.fetchSize(rowBounds.getLimit());
        }
        return statement;
    }

    /**
     * get r2dbc key generator
     *
     * @param mappedStatement MappedStatement
     * @return R2dbcKeyGenerator
     */
    private R2dbcKeyGenerator getR2dbcKeyGenerator(MappedStatement mappedStatement) {
        String[] keyColumns = mappedStatement.getKeyColumns();
        boolean hasKeyColumns = keyColumns != null && keyColumns.length != 0;
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        boolean useJdbc3KeyGenerator = keyGenerator instanceof Jdbc3KeyGenerator;
        // link to issue #55 , When using useGeneratedKeys="true" in insert sql, there should be a check for keyColumn="xxxx"
        if(useJdbc3KeyGenerator && !hasKeyColumns){
            throw new GeneratedKeysException("When useGeneratedKeys is configured to simply return the generated keys , " +
                    "the keyColumns must also be configured , please check @Options or xml 's keyColumns configuration.");
        }
        if (useJdbc3KeyGenerator) {
            return new DefaultR2dbcKeyGenerator(mappedStatement, super.configuration);
        }
        if (keyGenerator instanceof SelectKeyGenerator) {
            return new SelectR2dbcKeyGenerator((SelectKeyGenerator) keyGenerator, super.configuration, this);
        }
        return NoKeyR2dbcKeyGenerator.getInstance();
    }

}

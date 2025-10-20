package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.query;

import io.r2dbc.spi.Result;
import io.r2dbc.spi.Result.Message;
import io.r2dbc.spi.Result.OutSegment;
import io.r2dbc.spi.Result.RowSegment;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.session.RowBounds;
import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.parser.ResultRowDataParser;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.parser.ResultHandlerToolkit;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author Gang Cheng
 * @version 0.1.0
 * @since 3.1.0
 */
public class QueryResultHandler<R> {

    private static final Log log = LogFactory.getLog(QueryResultHandler.class);

    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final BoundSql boundSql;
    private final ParameterHandler parameterHandler;
    private final R2dbcStatementLog r2dbcStatementLog;

    private final ResultRowDataParser<R> resultRowDataParser;

    private final AtomicLong totalReceivedCount = new AtomicLong();
    private final boolean anyOutParameterExist;
    private final boolean isDefaultRowBounds;

    private QueryResultHandler(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                               MappedStatement mappedStatement,
                               RowBounds rowBounds,
                               BoundSql boundSql,
                               ParameterHandler parameterHandler,
                               R2dbcStatementLog r2dbcStatementLog) {
        this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.parameterHandler = parameterHandler;
        this.r2dbcStatementLog = r2dbcStatementLog;
        this.resultRowDataParser = new ResultRowDataParser<>(r2dbcMybatisConfiguration, mappedStatement);
        this.anyOutParameterExist = this.determineIfAnyOutParameterExist(boundSql);
        this.isDefaultRowBounds = this.determineIfDefaultRowBounds(rowBounds);
    }

    public static <R> QueryResultHandler<R> of(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                               MappedStatement mappedStatement,
                                               RowBounds rowBounds,
                                               BoundSql boundSql,
                                               ParameterHandler parameterHandler,
                                               R2dbcStatementLog r2dbcStatementLog) {
        return new QueryResultHandler<>(r2dbcMybatisConfiguration, mappedStatement, rowBounds, boundSql, parameterHandler, r2dbcStatementLog);
    }

    // ==== Methods for initialization start ====

    private boolean determineIfAnyOutParameterExist(BoundSql boundSql) {
        return boundSql.getParameterMappings()
                .stream()
                .anyMatch(parameterMapping ->
                        ParameterMode.OUT.equals(parameterMapping.getMode())
                                || ParameterMode.INOUT.equals(parameterMapping.getMode())
                );
    }

    private boolean determineIfDefaultRowBounds(RowBounds rowBounds) {
        return RowBounds.DEFAULT.equals(rowBounds);
    }

    // ==== Methods for initialization end ====

    public Flux<R> handle(Publisher<? extends Result> resultPublisher) {
        return Flux.from(resultPublisher)
                .concatMap(result -> {
                    if (this.anyOutParameterExist) {
                        return this.handleResultWithOutputParameters(result);
                    }
                    return this.handleResultWithoutOutputParameters(result);
                })
                .concatWith(Flux.defer(() -> Flux.create(sink -> {
                    Deque<Object> resultHolder = resultRowDataParser.getResultHolder();
                    if (!resultHolder.isEmpty()) {
                        for (Object result : resultHolder) {
                            if (Objects.nonNull(result)) {
                                sink.next((R) result);
                            }
                        }
                    }
                    sink.complete();
                })))
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> r2dbcStatementLog.logTotal(resultRowDataParser.getTotalCount()))
                .doFinally(signalType -> resultRowDataParser.cleanup());
    }

    // ==== Methods for parsing result start ====

    private Publisher<R> handleResultWithOutputParameters(Result value) {
        return value.filter(segment -> segment instanceof Message
                        || segment instanceof RowSegment
                        || segment instanceof OutSegment
                )
                .flatMap(segment -> {
                    if (segment instanceof Message) {
                        return Mono.error(((Message) segment).exception());
                    }
                    // output parameters
                    if (segment instanceof OutSegment) {
                        return ResultHandlerToolkit.handleOutputParameters((OutSegment) segment, r2dbcMybatisConfiguration, boundSql, parameterHandler)
                                .then(Mono.empty());
                    }
                    // row data
                    if (segment instanceof RowSegment) {
                        long receivedCount = totalReceivedCount.incrementAndGet();
                        if (!isDefaultRowBounds) {
                            if (receivedCount < rowBounds.getOffset()) {
                                log.debug("Row bounds startpoint strict to " + rowBounds.getOffset() + ", bypass processing current row data.");
                                return Mono.empty();
                            }
                            if (receivedCount > rowBounds.getOffset() + rowBounds.getLimit()) {
                                log.debug("Row bounds limit reached, stop parsing");
                                return Mono.empty();
                            }
                        } else if (log.isTraceEnabled()) {
                            // this would bypass the row bounds limitation check if row bounds are not set
                            // int this case the result count would be larger than Integer.MAX_VALUE until the sink is cancelled or disposed or error occurred
                            log.trace("Default row bounds, no need to check row bounds limit");
                        }
                        return this.parseRowSegment((RowSegment) segment);
                    }
                    log.trace("Ignore process result's segment : " + segment.getClass());
                    return Mono.empty();
                });
    }

    private Publisher<R> handleResultWithoutOutputParameters(Result value) {
        return value.filter(segment -> segment instanceof Message
                        || segment instanceof RowSegment
                )
                .flatMap(segment -> {
                    if (segment instanceof Message) {
                        return Mono.error(((Message) segment).exception());
                    }
                    return this.parseRowSegment((RowSegment) segment);
                });
    }

    private Mono<R> parseRowSegment(RowSegment segment) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(
                        () -> {
                            R resultValue = resultRowDataParser.handleResult(ReadableResultWrapper.ofRow(segment.row(), r2dbcMybatisConfiguration));
                            if (Objects.isNull(resultValue) && resultRowDataParser.getResultHolder().size() > 1) {
                                return (R) resultRowDataParser.getResultHolder().pop();
                            }
                            return resultValue;
                        }
                )
        );
    }

    // ==== Methods for parsing result end ====
}

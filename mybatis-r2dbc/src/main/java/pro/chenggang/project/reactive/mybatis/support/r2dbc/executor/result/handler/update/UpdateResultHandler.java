package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler.update;

import io.r2dbc.spi.Result;
import io.r2dbc.spi.Result.Message;
import io.r2dbc.spi.Result.OutSegment;
import io.r2dbc.spi.Result.RowSegment;
import io.r2dbc.spi.Row;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMode;
import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.R2dbcKeyGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.ReadableResultWrapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.parser.ResultHandlerToolkit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.KeyGeneratorType.SELECT_KEY_AFTER;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key.KeyGeneratorType.SIMPLE_RETURN;

/**
 * @author Gang Cheng
 * @version 0.1.0
 * @since 3.1.0
 */
public class UpdateResultHandler {

    private static final Log log = LogFactory.getLog(UpdateResultHandler.class);

    private final R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
    private final MappedStatement mappedStatement;
    private final Object parameter;
    private final BoundSql boundSql;
    private final ParameterHandler parameterHandler;
    private final R2dbcKeyGenerator r2dbcKeyGenerator;
    private final boolean anyOutParameterExist;
    private final boolean anyGeneratedKeyExist;

    private UpdateResultHandler(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                MappedStatement mappedStatement,
                                Object parameter,
                                BoundSql boundSql,
                                ParameterHandler parameterHandler,
                                R2dbcKeyGenerator r2dbcKeyGenerator) {
        this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
        this.mappedStatement = mappedStatement;
        this.parameter = parameter;
        this.boundSql = boundSql;
        this.parameterHandler = parameterHandler;
        this.r2dbcKeyGenerator = r2dbcKeyGenerator;
        this.anyOutParameterExist = this.determineIfAnyOutParameterExist(boundSql);
        this.anyGeneratedKeyExist = this.determineIfAnyGeneratedKeyExist();
    }

    public static UpdateResultHandler of(R2dbcMybatisConfiguration r2dbcMybatisConfiguration,
                                         MappedStatement mappedStatement,
                                         Object parameter,
                                         BoundSql boundSql,
                                         ParameterHandler parameterHandler,
                                         R2dbcKeyGenerator r2dbcKeyGenerator) {
        return new UpdateResultHandler(r2dbcMybatisConfiguration, mappedStatement, parameter, boundSql, parameterHandler, r2dbcKeyGenerator);
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

    private boolean determineIfAnyGeneratedKeyExist() {
        return SIMPLE_RETURN.equals(r2dbcKeyGenerator.keyGeneratorType());
    }

    // ==== Methods for initialization end ====

    public Mono<Long> handle(Publisher<? extends Result> resultPublisher) {
        return Flux.from(resultPublisher)
                .concatMap(result -> {
                    if (this.anyGeneratedKeyExist) {
                        return this.handleResultWithSimpleGeneratedKey(result);
                    }
                    if (this.anyOutParameterExist) {
                        return this.handleResultWithOutputParameters(result);
                    }
                    return result.getRowsUpdated();
                })
                .publishOn(Schedulers.boundedElastic())
                .reduce(Long::sum)
                .defaultIfEmpty(0L)
                .flatMap(totalUpdateRowCount -> r2dbcKeyGenerator.processSelectKey(SELECT_KEY_AFTER, mappedStatement, parameter)
                        .thenReturn(totalUpdateRowCount)
                );
    }

    // ==== Methods for processing result start ====

    private Publisher<Long> handleResultWithOutputParameters(Result value) {
        return value.filter(segment -> segment instanceof Message
                        || segment instanceof RowSegment
                        || segment instanceof OutSegment
                )
                .flatMap(segment -> {
                    if (segment instanceof Message) {
                        return Mono.error(((Message) segment).exception());
                    }
                    // row data
                    if (segment instanceof RowSegment) {
                        log.warn("Unsupported Row data during output parameter mapping." +
                                " To handle multiple rows of output parameters," +
                                " consider using a query operation rather than an update operation." +
                                " Receiving output parameters with an update operation is only effective for single-row results.");
                        return Mono.just(0L);
                    }
                    // unsupported segment type
                    if (!(segment instanceof OutSegment)) {
                        log.trace("Ignore process result's segment : " + segment.getClass());
                        return Mono.empty();
                    }
                    // output parameters
                    log.debug("Handle output parameters with segment: " + segment);
                    return ResultHandlerToolkit.handleOutputParameters((OutSegment) segment,
                            this.r2dbcMybatisConfiguration,
                            this.boundSql,
                            this.parameterHandler
                    );
                });
    }

    private Publisher<Long> handleResultWithSimpleGeneratedKey(Result value) {
        return value.map((row, rowMetadata) -> {
            log.trace("Handle simple generated key with row: " + row);
            ReadableResultWrapper<Row> readableResultWrapper = ReadableResultWrapper.ofRow(row, this.r2dbcMybatisConfiguration);
            return r2dbcKeyGenerator.processGeneratedKeyResult(readableResultWrapper, this.parameter);
        });
    }

    // ==== Methods for processing result end ====

}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.defaults;

import io.r2dbc.spi.IsolationLevel;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.RowBounds;
import org.reactivestreams.Publisher;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.ReactiveExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.StatementLogHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class DefaultReactiveSqlSession implements ReactiveSqlSession {

    private static final Log log = LogFactory.getLog(DefaultReactiveSqlSession.class);

    private final boolean autoCommit;
    private final R2dbcConfiguration configuration;
    private final ReactiveExecutor reactiveExecutor;
    private final IsolationLevel isolationLevel;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean withinTransaction = new AtomicBoolean(false);

    public DefaultReactiveSqlSession(R2dbcConfiguration configuration, ReactiveExecutor reactiveExecutor, boolean autoCommit, IsolationLevel isolationLevel) {
        this.configuration = configuration;
        this.reactiveExecutor = reactiveExecutor;
        this.autoCommit = autoCommit;
        this.isolationLevel = isolationLevel;
    }

    public DefaultReactiveSqlSession(R2dbcConfiguration configuration, ReactiveExecutor reactiveExecutor) {
        this(configuration,reactiveExecutor,false, null);
    }

    public DefaultReactiveSqlSession(R2dbcConfiguration configuration, ReactiveExecutor reactiveExecutor,IsolationLevel isolationLevel) {
        this(configuration,reactiveExecutor,false, isolationLevel);
    }

    @Override
    public Mono<Void> withTransaction() {
        if(this.withinTransaction.compareAndSet(false,true)){
            log.debug("ReactiveSqlSession operation start with transaction");
        }
        return Mono.empty();
    }

    @Override
    public <T> Mono<T> selectOne(String statement, Object parameter) {
        return this.<T>selectList(statement,parameter)
                .singleOrEmpty()
                .onErrorMap(IndexOutOfBoundsException.class, e -> new TooManyResultsException("Expected one result (or null) to be returned by selectOne()"));
    }

    @Override
    public <E> Flux<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return reactiveExecutor.<E>query(mappedStatement, wrapCollection(parameter), rowBounds)
                .contextWrite(context -> initReactiveExecutorContext(context,new StatementLogHelper(mappedStatement.getStatementLog())));
    }

    @Override
    public Mono<Integer> insert(String statement, Object parameter) {
        return this.update(statement,parameter);
    }

    @Override
    public Mono<Integer> update(String statement, Object parameter) {
        dirty.compareAndSet(false,true);
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return reactiveExecutor.update(mappedStatement, wrapCollection(parameter))
                .contextWrite(context -> initReactiveExecutorContext(context,new StatementLogHelper(mappedStatement.getStatementLog())));
    }

    @Override
    public Mono<Integer> delete(String statement, Object parameter) {
        return this.update(statement,parameter);
    }

    @Override
    public Mono<Void> commit(boolean force) {
        return reactiveExecutor.commit(isCommitOrRollbackRequired(force))
                .doOnSubscribe(s -> dirty.compareAndSet(true,false));
    }

    @Override
    public Mono<Void> rollback(boolean force) {
        return reactiveExecutor.rollback(isCommitOrRollbackRequired(force))
                .doOnSubscribe(s -> dirty.compareAndSet(true,false));
    }

    @Override
    public R2dbcConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return this.configuration.getMapper(type,this);
    }

    @Override
    public <T, V> Publisher<T> execute(Class<V> interfaceClass, BiFunction<ReactiveSqlSession, V, Publisher<T>> execution) {
        return execution.apply(this,this.getMapper(interfaceClass));
    }

    @Override
    public Mono<Void> close() {
        return reactiveExecutor.close(isCommitOrRollbackRequired(false))
                .doOnSubscribe(s -> dirty.compareAndSet(true,false));
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty.get()) || force;
    }

    private Object wrapCollection(final Object object) {
        return ParamNameResolver.wrapToMapIfCollection(object, null);
    }

    private Context initReactiveExecutorContext(Context context, StatementLogHelper statementLogHelper) {
        Optional<ReactiveExecutorContext> optionalContext = context.getOrEmpty(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext.class::cast);
        if(optionalContext.isPresent()){
            optionalContext.get().setUsingTransaction(this.withinTransaction);
            return context;
        }
        ReactiveExecutorContext newContext = new ReactiveExecutorContext(autoCommit, this.isolationLevel, statementLogHelper);
        newContext.setUsingTransaction(this.withinTransaction);
        return context.put(ReactiveExecutorContext.class,newContext);
    }
}

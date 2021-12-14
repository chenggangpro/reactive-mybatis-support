package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.IsolationLevel;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.RowBounds;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.StatementLogHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class DefaultReactiveSqlSession implements ReactiveSqlSession {

    private static final Log log = LogFactory.getLog(DefaultReactiveSqlSession.class);

    private final boolean autoCommit;
    private final R2dbcMybatisConfiguration configuration;
    private final ReactiveExecutor reactiveExecutor;
    private final IsolationLevel isolationLevel;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean withinTransaction = new AtomicBoolean(false);
    //due to the whenever create a ReactiveSqlSession ,only initialize executor once,
    //so just retain the connection Reference for the entire session context
    private final AtomicReference<Connection> connectionReference = new AtomicReference<>();
    private final AtomicBoolean sessionClosed = new AtomicBoolean(false);

    public DefaultReactiveSqlSession(R2dbcMybatisConfiguration configuration, ReactiveExecutor reactiveExecutor, boolean autoCommit, IsolationLevel isolationLevel) {
        this.configuration = configuration;
        this.reactiveExecutor = reactiveExecutor;
        this.autoCommit = autoCommit;
        this.isolationLevel = isolationLevel;
    }

    public DefaultReactiveSqlSession(R2dbcMybatisConfiguration configuration, ReactiveExecutor reactiveExecutor) {
        this(configuration,reactiveExecutor,false, null);
    }

    public DefaultReactiveSqlSession(R2dbcMybatisConfiguration configuration, ReactiveExecutor reactiveExecutor, IsolationLevel isolationLevel) {
        this(configuration,reactiveExecutor,false, isolationLevel);
    }

    @Override
    public ReactiveSqlSession withTransaction() {
        this.checkSessionClosed();
        if(this.withinTransaction.compareAndSet(false,true)){
            log.debug("ReactiveSqlSession operation start with transaction");
        }
        return this;
    }

    @Override
    public <T> Mono<T> selectOne(String statement, Object parameter) {
        return this.<T>selectList(statement,parameter)
                .singleOrEmpty()
                .onErrorMap(IndexOutOfBoundsException.class, e -> new TooManyResultsException("Expected one result (or null) to be returned by selectOne()"));
    }

    @Override
    public <E> Flux<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        this.checkSessionClosed();
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
        this.checkSessionClosed();
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
        this.checkSessionClosed();
        return reactiveExecutor.commit(isCommitOrRollbackRequired(force))
                .contextWrite(this::initReactiveExecutorContext)
                .doOnSubscribe(s -> dirty.compareAndSet(true,false));
    }

    @Override
    public Mono<Void> rollback(boolean force) {
        this.checkSessionClosed();
        return reactiveExecutor.rollback(isCommitOrRollbackRequired(force))
                .contextWrite(this::initReactiveExecutorContext)
                .doOnSubscribe(s -> dirty.compareAndSet(true,false));
    }

    @Override
    public R2dbcMybatisConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return this.configuration.getMapper(type,this);
    }

    @Override
    public Mono<Void> close() {
        this.checkSessionClosed();
        return reactiveExecutor.close(isCommitOrRollbackRequired(false))
                .contextWrite(this::initReactiveExecutorContext)
                .doOnSubscribe(s -> {
                    dirty.compareAndSet(true, false);
                    sessionClosed.compareAndSet(false,true);
                });
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty.get()) || force;
    }

    private Object wrapCollection(final Object object) {
        return ParamNameResolver.wrapToMapIfCollection(object, null);
    }

    private void checkSessionClosed(){
        if(sessionClosed.get()){
            throw new IllegalStateException("Reactive session already closed");
        }
    }

    private Context initReactiveExecutorContext(Context context, StatementLogHelper statementLogHelper) {
        Optional<ReactiveExecutorContext> optionalContext = context.getOrEmpty(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext.class::cast);
        if(optionalContext.isPresent()){
            optionalContext.get().setUsingTransaction(this.withinTransaction);
            return context;
        }
        ReactiveExecutorContext newContext = new ReactiveExecutorContext(autoCommit, this.isolationLevel, connectionReference);
        newContext.setStatementLogHelper(statementLogHelper);
        newContext.setUsingTransaction(this.withinTransaction);
        return context.put(ReactiveExecutorContext.class,newContext);
    }

    private Context initReactiveExecutorContext(Context context) {
        Optional<ReactiveExecutorContext> optionalContext = context.getOrEmpty(ReactiveExecutorContext.class)
                .map(ReactiveExecutorContext.class::cast);
        if(optionalContext.isPresent()){
            if(log.isTraceEnabled()){
                log.trace("Initialize reactive executor context,context already exist :" + optionalContext);
            }
            optionalContext.get().setUsingTransaction(this.withinTransaction);
            return context;
        }
        if(log.isTraceEnabled()){
            log.trace("Initialize reactive executor context,context not exist,create new one");
        }
        ReactiveExecutorContext newContext = new ReactiveExecutorContext(autoCommit, this.isolationLevel, connectionReference);
        newContext.setUsingTransaction(this.withinTransaction);
        return context.put(ReactiveExecutorContext.class,newContext);
    }
}

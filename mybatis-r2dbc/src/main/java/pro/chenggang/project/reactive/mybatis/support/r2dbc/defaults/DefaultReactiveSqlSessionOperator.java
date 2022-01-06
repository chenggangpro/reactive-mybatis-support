package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextHelper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Default reactive sql session operator.
 *
 * @author chenggang
 * @date 12 /16/21.
 */
public class DefaultReactiveSqlSessionOperator implements ReactiveSqlSessionOperator {

    private final ReactiveSqlSession reactiveSqlSession;
    private final MybatisReactiveContextHelper mybatisReactiveContextHelper;

    /**
     * Instantiates a new Default reactive sql session operator.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     */
    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory) {
        this(reactiveSqlSessionFactory, false);
    }

    /**
     * Instantiates a new Default reactive sql session operator.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     * @param enableTransaction         the enable transaction
     */
    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory, boolean enableTransaction) {
        this.reactiveSqlSession = reactiveSqlSessionFactory.openSession().usingTransaction(enableTransaction);
        this.mybatisReactiveContextHelper = (MybatisReactiveContextHelper) this.reactiveSqlSession;
    }

    @Override
    public <T> Mono<T> execute(Mono<T> monoExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> this.reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Mono<T> executeAndCommit(Mono<T> monoExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Mono<T> executeAndRollback(Mono<T> monoExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMap(reactiveExecutorContext -> Mono.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> monoExecution,
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Flux<T> executeMany(Flux<T> fluxExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        ReactiveSqlSession::close,
                        (session, err) -> Mono.empty(),
                        ReactiveSqlSession::close
                ).onErrorResume(ex -> this.reactiveSqlSession.close()
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Flux<T> executeManyAndCommit(Flux<T> fluxExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Flux<T> executeManyAndRollback(Flux<T> fluxExecution) {
        return MybatisReactiveContextHelper.currentContext()
                .flatMapMany(reactiveExecutorContext -> Flux.usingWhen(
                        Mono.just(reactiveSqlSession),
                        session -> fluxExecution,
                        session -> session.commit(true)
                                .then(Mono.defer(session::close)),
                        (session, err) -> Mono.empty(),
                        session -> session.rollback(true)
                                .then(Mono.defer(session::close))
                ).onErrorResume(ex -> this.reactiveSqlSession.rollback(true)
                        .then(Mono.defer(this.reactiveSqlSession::close))
                        .then(Mono.defer(() -> Mono.error(ex)))
                ))
                .contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }
}

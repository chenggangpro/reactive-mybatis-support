package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextHelper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionOperator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveExecutorContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 12/16/21.
 */
public class DefaultReactiveSqlSessionOperator implements ReactiveSqlSessionOperator {

    private final ReactiveSqlSession reactiveSqlSession;
    private final MybatisReactiveContextHelper mybatisReactiveContextHelper;

    public DefaultReactiveSqlSessionOperator(ReactiveSqlSessionFactory reactiveSqlSessionFactory) {
        this.reactiveSqlSession = reactiveSqlSessionFactory.openSession().withTransaction();
        this.mybatisReactiveContextHelper = (MybatisReactiveContextHelper) this.reactiveSqlSession;
    }

    @Override
    public <T> Mono<T> executeAndCommit(Mono<T> monoExecution) {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
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
                ))).contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Mono<T> executeAndRollback(Mono<T> monoExecution) {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
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
                ))).contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Flux<T> executeManyAndCommit(Flux<T> fluxExecution) {
        return Flux.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
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
                ))).contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }

    @Override
    public <T> Flux<T> executeManyAndRollback(Flux<T> fluxExecution) {
        return Flux.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .cast(ReactiveExecutorContext.class)
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
                ))).contextWrite(mybatisReactiveContextHelper::initReactiveExecutorContext);
    }
}

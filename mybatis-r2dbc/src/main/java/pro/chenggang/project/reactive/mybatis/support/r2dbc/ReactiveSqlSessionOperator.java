package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Reactive sql session operator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @date 12/16/21.
 */
public interface ReactiveSqlSessionOperator {

    /**
     * execute with Mono
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> execute(Mono<T> monoExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndCommit(Mono<T> monoExecution);

    /**
     * execute with Mono then rollback
     *
     * @param <T>           the type parameter
     * @param monoExecution the mono execution
     * @return mono
     */
    <T> Mono<T> executeAndRollback(Mono<T> monoExecution);

    /**
     * execute with Mono then commit
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeMany(Flux<T> fluxExecution);

    /**
     * execute with Flux
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndCommit(Flux<T> fluxExecution);

    /**
     * execute with Flux then rollback
     *
     * @param <T>           the type parameter
     * @param fluxExecution the flux execution
     * @return flux
     */
    <T> Flux<T> executeManyAndRollback(Flux<T> fluxExecution);

}

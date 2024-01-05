package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service;

import reactor.core.publisher.Mono;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DynamicRoutingService {

    Mono<Void> runWithDynamicRoutingWithoutTransaction();

    Mono<Void> runWithDynamicRoutingWithTransactionCommit();

    Mono<Void> runWithDynamicRoutingWithTransactionRollback();

}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service;

import reactor.core.publisher.Mono;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ApplicationService {

    Mono<Void> runWithoutTransaction();

    Mono<Void> runWithTransactionCommit();

    Mono<Void> runWithTransactionRollback();
}

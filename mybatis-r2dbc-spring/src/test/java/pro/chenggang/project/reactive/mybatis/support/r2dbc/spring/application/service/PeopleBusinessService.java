package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.People;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 7/5/21.
 */
public interface PeopleBusinessService {

    Mono<People> doWithTransactionBusiness();

    Mono<People> doWithTransactionBusinessRollback();

    Mono<Integer> doInsertPerformance(People people);

    Flux<People> doSelectAllPerformance();
}

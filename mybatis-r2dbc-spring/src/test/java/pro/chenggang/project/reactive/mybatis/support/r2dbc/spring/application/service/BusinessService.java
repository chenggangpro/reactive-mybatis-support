package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend.DeptWithEmp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import reactor.core.publisher.Mono;

/**
 * @author Gang Cheng
 */
public interface BusinessService {

    Mono<Dept> doWithTransactionBusiness();

    Mono<Dept> doWithTransactionBusinessRollback();

    Mono<DeptWithEmp> doWithoutTransaction();

}

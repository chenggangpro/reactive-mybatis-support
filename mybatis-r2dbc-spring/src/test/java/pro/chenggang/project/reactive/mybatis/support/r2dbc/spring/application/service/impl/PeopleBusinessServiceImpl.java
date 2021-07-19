package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.People;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.PeopleMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.PeopleBusinessService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.PeopleDynamicSqlSupport.id;

/**
 * @author: chenggang
 * @date 7/5/21.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PeopleBusinessServiceImpl implements PeopleBusinessService {

    private final PeopleMapper peopleMapper;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Mono<People> doWithTransactionBusiness() {
        return this.doBusinessInternal();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Mono<People> doWithTransactionBusinessRollback() {
        return this.doBusinessInternal()
                .then(Mono.error(new RuntimeException("manually throw runtime exception")));
    }

    @Override
    public Mono<Integer> doInsertPerformance(People people) {
        people.setId(null);
        people.setCreatedAt(LocalDateTime.now());
        return this.peopleMapper.insertSelective(people);
    }

    @Override
    public Flux<People> doSelectAllPerformance() {
        return this.peopleMapper.count(dsl -> dsl)
                .flatMapMany(totalcount -> {
                    int offset = RandomUtils.nextInt(0, (totalcount.intValue() - 100 ) <=0 ? totalcount.intValue() : (totalcount.intValue() - 100));
                    return this.peopleMapper.select(dsl -> dsl.orderBy(id).limit(100).offset(offset));
                });
    }

    private Mono<People> doBusinessInternal(){
        return peopleMapper.selectOne(dsl -> dsl.where(id, isEqualTo(1)))
                .doOnNext(people -> log.debug("[Before] Get People ,People:{}",people))
                .flatMap(people -> peopleMapper.updateSelective(new People().setNick("update_nick"),dsl -> dsl.where(id,isEqualTo(1))))
                .flatMap(value -> peopleMapper.selectOne(dsl -> dsl.where(id, isEqualTo(1))))
                .doOnNext(updatePeople -> log.debug("[After Update] Get People ,People:{}",updatePeople))
                .flatMap(updatePeople -> peopleMapper.delete(dsl -> dsl.where(id,isEqualTo(1))))
                .flatMap(deleteResult -> peopleMapper.selectOne(dsl -> dsl.where(id, isEqualTo(1))));
    }
}

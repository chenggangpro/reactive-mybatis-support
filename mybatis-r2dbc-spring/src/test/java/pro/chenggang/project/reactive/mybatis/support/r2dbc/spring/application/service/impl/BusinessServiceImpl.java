package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.createTime;
import static pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic.DeptDynamicSqlSupport.deptNo;

/**
 * @author: chenggang
 * @date 7/5/21.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final DeptMapper deptMapper;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Mono<Dept> doWithTransactionBusiness() {
        return this.doBusinessInternal();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Mono<Dept> doWithTransactionBusinessRollback() {
        return this.doBusinessInternal()
                .then(Mono.error(new RuntimeException("manually throw runtime exception")));
    }

    @Override
    public Mono<Integer> doInsertPerformance(Dept dept) {
        dept.setDeptNo(null);
        dept.setDeptName(dept.getDeptName());
        dept.setLocation(dept.getLocation());
        dept.setCreateTime(LocalDateTime.now());
        return this.deptMapper.insertSelective(dept);
    }

    @Override
    public Flux<Dept> doSelectAllPerformance() {
        return this.deptMapper.count(dsl -> dsl)
                .flatMapMany(totalCount -> {
                    int offset = RandomUtils.nextInt(0, (totalCount.intValue() - 100 ) <=0 ? totalCount.intValue() : (totalCount.intValue() - 100));
                    return this.deptMapper.select(dsl -> dsl.orderBy(createTime).limit(100).offset(offset));
                });
    }

    private Mono<Dept> doBusinessInternal(){
        return deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L)))
                .doOnNext(people -> log.debug("[Before] Get People ,People:{}",people))
                .flatMap(people -> deptMapper.updateSelective(new Dept()
                        .setDeptName("InsertDept")
                        .setLocation("InsertLocation")
                        .setCreateTime(LocalDateTime.now()),
                        dsl -> dsl.where(deptNo,isEqualTo(4L))))
                .flatMap(value -> deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L))))
                .doOnNext(updatePeople -> log.debug("[After Update] Get People ,People:{}",updatePeople))
                .flatMap(updatePeople -> deptMapper.delete(dsl -> dsl.where(deptNo,isEqualTo(4L))))
                .flatMap(deleteResult -> deptMapper.selectOne(dsl -> dsl.where(deptNo, isEqualTo(4L))));
    }
}

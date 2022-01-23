package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BenchmarkService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author Gang Cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenchmarkServiceImpl implements BenchmarkService {

    private final DeptMapper deptMapper;

    @Override
    public Mono<?> getFromDb() {
        return deptMapper.selectDeptWithEmpList()
                .collectList();
    }

    @Override
    public Mono<?> getFromDb(String id) {
        return deptMapper.selectOneByDeptNo(Long.parseLong(id))
                .cast(Object.class)
                .switchIfEmpty(Mono.defer(() -> Mono.just("data not exist")));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Mono<?> insertDb() {
        Dept dept = new Dept();
        dept.setDeptName(IdUtil.objectId());
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation(IdUtil.objectId());
        return deptMapper.insertSelective(dept)
                .flatMap(insertResult -> Mono.just("Generated DeptNo: " + dept.getDeptNo()));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Mono<?> insertThenDeleteDb() {
        Dept dept = new Dept();
        dept.setDeptName(IdUtil.objectId());
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation(IdUtil.objectId());
        return deptMapper.insertSelective(dept)
                .flatMap(insertResult -> deptMapper.deleteByDeptNo(dept.getDeptNo())
                        .flatMap(deleteResult -> deptMapper.count()
                                .map(countResult -> "TotalCount : " + countResult))
                );
    }
}

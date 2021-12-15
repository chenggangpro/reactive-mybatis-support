package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.mapper.EmpMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.setup.MybatisR2dbcBaseTests;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: chenggang
 * @date 12/15/21.
 */
public class TransactionMapperTests extends MybatisR2dbcBaseTests {

    private Map<Class<?>,ReactiveSqlSession> reactiveSqlSessionMap = new HashMap<>();
    private DeptMapper deptMapper;
    private EmpMapper empMapper;

    @BeforeAll
    public void initSqlSession () throws Exception {
        this.deptMapper = this.getMapperWithSession(DeptMapper.class);
        this.empMapper = this.getMapperWithSession(EmpMapper.class);
    }

    private <T> T getMapperWithSession(Class<T> targetClass){
        ReactiveSqlSession reactiveSqlSession = super.reactiveSqlSessionFactory.openSession();
        this.reactiveSqlSessionMap.put(targetClass,reactiveSqlSession);
        return reactiveSqlSession.withTransaction().getMapper(targetClass);
    }

    @Test
    public void testManuallyCommit() throws Exception {
        Dept dept = new Dept();
        dept.setDeptName("Test_dept_name");
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation("Test_location");
        Mono<Void> monoExecution = this.deptMapper.count()
                .flatMap(totalCount -> {
                    assertThat(totalCount).isEqualTo(4);
                    return this.deptMapper.insert(dept);
                })
                .flatMap(effectRowCount -> {
                    assertThat(effectRowCount).isEqualTo(1);
                    return this.deptMapper.count();
                })
                .flatMap(totalCount -> {
                    assertThat(totalCount).isEqualTo(5);
                    ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionMap.get(DeptMapper.class);
                    return reactiveSqlSession
                            .commit(true);
                })
                .then(Mono.defer(() -> this.deptMapper.count()
                        .flatMap(totalCount -> {
                            assertThat(totalCount).isEqualTo(5);
                            return this.deptMapper.deleteByDeptNo(dept.getDeptNo());
                        })
                        .flatMap(effectRowCount -> {
                            assertThat(effectRowCount).isEqualTo(1);
                            return this.deptMapper.count();
                        })
                        .flatMap(count -> {
                            assertThat(count).isEqualTo(4);
                            ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionMap.get(DeptMapper.class);
                            return reactiveSqlSession
                                    .commit(true)
                                    .then(Mono.defer(() -> reactiveSqlSession.close()));
                        })
                ));
        StepVerifier.create(monoExecution)
                .verifyComplete();
    }

    @Test
    public void testManuallyRollback() throws Exception {
        Dept dept = new Dept();
        dept.setDeptName("Test_dept_name");
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation("Test_location");
        Mono<Void> monoExecution = this.deptMapper.count()
                .flatMap(totalCount -> {
                    assertThat(totalCount).isEqualTo(4);
                    return this.deptMapper.insert(dept);
                })
                .flatMap(effectRowCount -> {
                    assertThat(effectRowCount).isEqualTo(1);
                    return this.deptMapper.count();
                })
                .flatMap(totalCount -> {
                    assertThat(totalCount).isEqualTo(5);
                    ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionMap.get(DeptMapper.class);
                    return reactiveSqlSession
                            .rollback(true);
                })
                .then(Mono.defer(() -> this.deptMapper.count()
                        .flatMap(totalCount -> {
                            assertThat(totalCount).isEqualTo(4);
                            ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionMap.get(DeptMapper.class);
                            return reactiveSqlSession
                                    .commit(true)
                                    .then(Mono.defer(() -> reactiveSqlSession.close()));
                        })
                ));
        StepVerifier.create(monoExecution)
                .verifyComplete();
    }


}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.extend.DeptWithEmp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.entity.model.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.application.mapper.EmpMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.setup.MybatisR2dbcBaseTests;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: chenggang
 * @date 12/15/21.
 */
@Slf4j
public class SimpleMapperTests extends MybatisR2dbcBaseTests {

    private ReactiveSqlSession reactiveSqlSession;
    private DeptMapper deptMapper;
    private EmpMapper empMapper;

    @BeforeAll
    public void initSqlSession () throws Exception {
        this.reactiveSqlSession = super.reactiveSqlSessionFactory.openSession();
        this.deptMapper = this.reactiveSqlSession.withTransaction().getMapper(DeptMapper.class);
        this.empMapper = this.reactiveSqlSession.withTransaction().getMapper(EmpMapper.class);
    }

    @AfterAll
    public void rollbackAndCloseSession () throws Exception {
        StepVerifier.create(reactiveSqlSession.rollback(true))
                .verifyComplete();
    }

    @Test
    public void testGetAllDept () throws Exception {
        StepVerifier.create(this.deptMapper.selectAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void testGetDeptByDeptNo () throws Exception {
        Long deptNo = 1L;
        StepVerifier.create(this.deptMapper.selectOneByDeptNo(deptNo))
                .expectNextMatches(dept -> deptNo.equals(dept.getDeptNo()))
                .verifyComplete();
    }

    @Test
    public void testGetDeptListByCreateTime () throws Exception {
        LocalDateTime createTime = LocalDateTime.now();
        StepVerifier.create(this.deptMapper.selectListByTime(createTime))
                .thenConsumeWhile(result -> {
                    assertThat(result)
                            .extracting(dept -> dept.getCreateTime().toLocalDate())
                            .matches(dateTime -> createTime.toLocalDate().equals(dateTime));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testInsertAndReturnGenerateKey() throws Exception{
        Dept dept = new Dept();
        dept.setDeptName("Test_dept_name");
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation("Test_location");
        StepVerifier.create(this.deptMapper.insert(dept))
                .expectNextMatches(effectRowCount -> effectRowCount == 1)
                .verifyComplete();
        assertThat(dept.getDeptNo()).isNotNull();
    }

    @Test
    public void testDeleteByDeptNo() throws Exception {
        Dept dept = new Dept();
        dept.setDeptName("Test_dept_name");
        dept.setCreateTime(LocalDateTime.now());
        dept.setLocation("Test_location");
        Mono<Integer> executionMono = this.deptMapper.insert(dept)
                .then(Mono.defer(() -> deptMapper.deleteByDeptNo(dept.getDeptNo())));
        StepVerifier.create(executionMono)
                .expectNextMatches(effectRowCount -> effectRowCount == 1)
                .verifyComplete();
    }

    @Test
    public void testUpdateByDeptNo() throws Exception {
        Dept dept = new Dept();
        dept.setDeptNo(1L);
        dept.setDeptName("Update_dept_name");
        StepVerifier.create(this.deptMapper.updateByDeptNo(dept))
                .expectNextMatches(effectRowCount -> effectRowCount == 1)
                .verifyComplete();
    }

    @Test
    public void testGetDeptWithEmp() throws Exception {
        StepVerifier.create(this.deptMapper.selectDeptWithEmpList())
                .expectNextMatches(deptWithEmp -> {
                    assertThat(deptWithEmp)
                            .extracting(DeptWithEmp::getEmpList)
                            .matches(empList -> empList.size() >0 );
                    return true;
                })
                .expectNextCount(2L)
                .expectNextMatches(deptWithEmp -> {
                    assertThat(deptWithEmp)
                            .extracting(DeptWithEmp::getEmpList)
                            .matches(empList -> empList.size() ==0 );
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testGetEmpWithDept() throws Exception {
        StepVerifier.create(this.empMapper.selectEmpWithDeptList())
                .thenConsumeWhile(empWithDept -> {
                    assertThat(empWithDept.getDept()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.DeptMapper;
import pro.chenggang.project.reactive.mybatis.support.common.mapper.EmpMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class BindingMapperTests extends MybatisR2dbcBaseTests {

    ReactiveSqlSessionFactory reactiveSqlSessionFactory;
    ReactiveSqlSession reactiveSqlSession;

    @BeforeEach
    void beforeEach() throws Exception {
        this.reactiveSqlSessionFactory = setUp(MySQLContainer.class, true, r2dbcProtocol -> {
            R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
            r2dbcMybatisConfiguration.addMappers("pro.chenggang.project.reactive.mybatis.support.common.mapper");
            return r2dbcMybatisConfiguration;
        });
        reactiveSqlSession = reactiveSqlSessionFactory.openSession();
    }

    @Test
    void getMapper() {
        DeptMapper deptMapper = reactiveSqlSession.getMapper(DeptMapper.class);
        Assertions.assertNotNull(deptMapper);
        EmpMapper empMapper = reactiveSqlSession.getMapper(EmpMapper.class);
        Assertions.assertNotNull(empMapper);
    }

}

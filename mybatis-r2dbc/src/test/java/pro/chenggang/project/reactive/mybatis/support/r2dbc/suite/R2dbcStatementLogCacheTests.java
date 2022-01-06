package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.setup.MybatisR2dbcBaseTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author chenggang
 * @date 2022/1/6.
 * @since 1.0.0
 */
public class R2dbcStatementLogCacheTests extends MybatisR2dbcBaseTests {

    @Test
    public void testR2dbcStatementLogFactory(){
        R2dbcStatementLogFactory r2dbcStatementLogFactory = super.r2dbcMybatisConfiguration.getR2dbcStatementLogFactory();
        assertThat(r2dbcStatementLogFactory.getAllR2dbcStatementLog()).isNotEmpty();
    }

    @Test
    public void testR2dbcStatementLog(){
        super.r2dbcMybatisConfiguration.getMappedStatements()
                        .forEach(mappedStatement -> {
                            R2dbcStatementLog r2dbcStatementLog = super.r2dbcMybatisConfiguration.getR2dbcStatementLog(mappedStatement);
                            assertThat(r2dbcStatementLog).isNotNull();
                        });
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.delete;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
class DeleteMapperTest extends MybatisR2dbcBaseTests {

    @Test
    void deleteByDeptNo() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNo(4L);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void deleteByDeptNoWithAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNoWithAnnotation(4L);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void deleteByDeptNoWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNoWithDynamic(4L);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }
}
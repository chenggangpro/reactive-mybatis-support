package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import reactor.test.StepVerifier;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void testCount() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper reactiveSqlSessionMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    reactiveSqlSessionMapper.countAll()
                            .as(StepVerifier::create)
                            .expectNext(4L)
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectOne() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper reactiveSqlSessionMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    reactiveSqlSessionMapper.selectOne()
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNo() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper reactiveSqlSessionMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    reactiveSqlSessionMapper.selectByDeptNo(1L)
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNoWithAnnotation() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper reactiveSqlSessionMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    reactiveSqlSessionMapper.selectByDeptNoWithAnnotatedResult(1L)
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNoWithResultMap() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper reactiveSqlSessionMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    reactiveSqlSessionMapper.selectByDeptNoWithResultMap(1L)
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

}

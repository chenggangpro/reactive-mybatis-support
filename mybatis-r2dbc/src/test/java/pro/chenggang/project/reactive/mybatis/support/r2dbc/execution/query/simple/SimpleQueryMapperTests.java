/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
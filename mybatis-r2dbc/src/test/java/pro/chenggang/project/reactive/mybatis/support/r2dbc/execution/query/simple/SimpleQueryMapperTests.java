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
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void countAll() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.countAllDept()
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
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectOneDept()
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
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
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectByDeptNo(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
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
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectByDeptNoWithAnnotatedResult(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
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
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectByDeptNoWithResultMap(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNoWithConstructMap() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectByDeptNoWithConstructorResultMap(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNoWithAnnotatedConstruct() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectByDeptNoWithAnnotatedConstructor(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectDeptWithEmpList() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectDeptWithEmpList(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                Assertions.assertEquals(dept.getDeptNo(), 1L);
                                Assertions.assertNotNull(dept.getEmpList());
                                Assertions.assertEquals(dept.getEmpList().size(), 3);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectEmpWithDept() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    simpleQueryMapper.selectEmpWithDept(1L)
                            .as(StepVerifier::create)
                            .assertNext(emp -> {
                                Assertions.assertEquals(emp.getEmpNo(), 1L);
                                Assertions.assertNotNull(emp.getDept());
                                Assertions.assertEquals(emp.getDept().getDeptNo(), 2L);
                            })
                            .verifyComplete();
                }
        );
    }


}

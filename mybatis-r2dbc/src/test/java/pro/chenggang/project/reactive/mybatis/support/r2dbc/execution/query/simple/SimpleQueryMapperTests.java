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
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.DeptWithEmpList;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.EmpWithDept;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimpleQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void countAll() {
        super.<Long>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.countAllDept();
                })
                .verifyWith(firstStep -> firstStep
                        .expectNext(4L)
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectOne() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectOneDept();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNo() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectByDeptNo(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNoWithAnnotation() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectByDeptNoWithAnnotatedResult(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNoWithResultMap() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectByDeptNoWithResultMap(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNoWithConstructMap() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectByDeptNoWithConstructorResultMap(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNoWithAnnotatedConstruct() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectByDeptNoWithAnnotatedConstructor(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectDeptWithEmpList() {
        super.<DeptWithEmpList>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectDeptWithEmpList(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(dept.getDeptNo(), 1L);
                            Assertions.assertNotNull(dept.getEmpList());
                            assertEquals(dept.getEmpList()
                                    .size(), 3);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectEmpWithDept() {
        super.<EmpWithDept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                    return simpleQueryMapper.selectEmpWithDept(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(emp.getEmpNo(), 1L);
                            Assertions.assertNotNull(emp.getDept());
                            assertEquals(emp.getDept()
                                    .getDeptNo(), 2L);
                        })
                        .verifyComplete()
                )
                .run();
    }


}

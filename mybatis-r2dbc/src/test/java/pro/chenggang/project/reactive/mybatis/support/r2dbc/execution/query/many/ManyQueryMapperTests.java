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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.many;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class ManyQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void selectAllDept(){
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllDept("TI")
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),1L);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),4L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllDeptWithEmpList(){
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllDeptWithEmpList()
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),1L);
                                assertNotNull(dept.getEmpList());
                                assertEquals(dept.getEmpList().size(),3);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),2L);
                                assertNotNull(dept.getEmpList());
                                assertEquals(dept.getEmpList().size(),5);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),3L);
                                assertNotNull(dept.getEmpList());
                                assertEquals(dept.getEmpList().size(),6);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),4L);
                                assertNotNull(dept.getEmpList());
                                assertTrue(dept.getEmpList().isEmpty());
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllDeptWithEmpListWithoutOrdered(){
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 15
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllDeptWithEmpList()
                            .take(1,true)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),1L);
                                assertNotNull(dept.getEmpList());
                                assertEquals(dept.getEmpList().size(),3);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllDeptWithEmpListOrdered(){
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 4
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllDeptWithEmpListOrdered()
                            .take(1,true) // take count
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(),1L);
                                assertNotNull(dept.getEmpList());
                                assertEquals(dept.getEmpList().size(),3);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllEmpWithOrdered(){
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllEmpWithOrdered("S")
                            .take(1,true) // take count
                            .as(StepVerifier::create)
                            .assertNext(emp -> {
                                assertEquals(emp.getEmpNo(),1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllEmpWithDept(){
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllEmpWithDept()
                            .as(StepVerifier::create)
                            .assertNext(emp -> {
                                assertEquals(emp.getEmpNo(),1L);
                                assertNotNull(emp.getDept());
                                assertEquals(emp.getDept().getDeptNo(),2L);
                            })
                            .assertNext(emp -> {
                                assertEquals(emp.getEmpNo(),2L);
                                assertNotNull(emp.getDept());
                                assertEquals(emp.getDept().getDeptNo(),3L);
                            })
                            .expectNextCount(12)
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllEmpWithDeptWithoutOrdered(){
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 14
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllEmpWithDept()
                            .take(1,true) // take count
                            .as(StepVerifier::create)
                            .assertNext(emp -> {
                                assertEquals(emp.getEmpNo(),1L);
                                assertNotNull(emp.getDept());
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllEmpWithDeptOrdered(){
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                },
                (type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    manyQueryMapper.selectAllEmpWithDeptOrdered()
                            .take(1,true) // take count
                            .as(StepVerifier::create)
                            .assertNext(emp -> {
                                assertEquals(emp.getEmpNo(),1L);
                                assertNotNull(emp.getDept());
                            })
                            .verifyComplete();
                }
        );
    }


}

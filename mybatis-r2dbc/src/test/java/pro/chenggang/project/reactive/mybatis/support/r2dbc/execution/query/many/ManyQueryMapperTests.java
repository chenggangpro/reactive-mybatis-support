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
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Emp;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.DeptWithEmpList;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.EmpWithDept;

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
    void selectAllDept() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllDept("TI");
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                        })
                        .assertNext(dept -> {
                            assertEquals(4L, dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllDeptWithEmpList() {
        super.<DeptWithEmpList>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllDeptWithEmpList();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertEquals(3, dept.getEmpList()
                                    .size());
                        })
                        .assertNext(dept -> {
                            assertEquals(2L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertEquals(5, dept.getEmpList()
                                    .size());
                        })
                        .assertNext(dept -> {
                            assertEquals(3L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertEquals(6, dept.getEmpList()
                                    .size());
                        })
                        .assertNext(dept -> {
                            assertEquals(4L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertTrue(dept.getEmpList()
                                    .isEmpty());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllDeptWithEmpListWithoutOrdered() {
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 15
        super.<DeptWithEmpList>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllDeptWithEmpList()
                            .take(1, true);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertEquals(3, dept.getEmpList()
                                    .size());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllDeptWithEmpListOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 4
        super.<DeptWithEmpList>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllDeptWithEmpListOrdered()
                            .take(1, true);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                            assertNotNull(dept.getEmpList());
                            assertEquals(3, dept.getEmpList()
                                    .size());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllEmpWithOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        super.<Emp>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllEmpWithOrdered("S")
                            .take(1, true);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(1L, emp.getEmpNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllEmpWithDept() {
        super.<EmpWithDept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllEmpWithDept();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(1L, emp.getEmpNo());
                            assertNotNull(emp.getDept());
                            assertEquals(2L, emp.getDept()
                                    .getDeptNo());
                        })
                        .assertNext(emp -> {
                            assertEquals(2L, emp.getEmpNo());
                            assertNotNull(emp.getDept());
                            assertEquals(3L, emp.getDept()
                                    .getDeptNo());
                        })
                        .expectNextCount(12)
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllEmpWithDeptWithoutOrdered() {
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 14
        super.<EmpWithDept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllEmpWithDept()
                            .take(1, true); // take count;
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(1L, emp.getEmpNo());
                            assertNotNull(emp.getDept());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllEmpWithDeptOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        super.<EmpWithDept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ManyQueryMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ManyQueryMapper manyQueryMapper = reactiveSqlSession.getMapper(ManyQueryMapper.class);
                    return manyQueryMapper.selectAllEmpWithDeptOrdered()
                            .take(1, true); // take count;
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(1L, emp.getEmpNo());
                            assertNotNull(emp.getDept());
                        })
                        .verifyComplete()
                )
                .run();
    }


}

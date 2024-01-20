/*
 *    Copyright 2009-2024 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.procedure;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Emp;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProcedureMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void callInoutProcedureUsingUpdate() {
        super.<SimpleRowProcedureData>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callInoutProcedureUsingUpdate(simpleRowProcedureData)
                            .thenReturn(simpleRowProcedureData);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> {
                            assertEquals(3L, result.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callInoutProcedureUsingSelect() {
        super.<SimpleRowProcedureData>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callInoutProcedureUsingSelect(simpleRowProcedureData)
                            .thenReturn(simpleRowProcedureData);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> {
                            assertEquals(3L, result.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callOutputProcedureUsingUpdate() {
        super.<SimpleRowProcedureData>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callOutputProcedureUsingUpdate(simpleRowProcedureData)
                            .thenReturn(simpleRowProcedureData);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> {
                            assertEquals("RESEARCH", result.getDeptName());
                            assertEquals("DALLAS", result.getLocation());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callOutputProcedureUsingSelect() {
        super.<SimpleRowProcedureData>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callOutputProcedureUsingSelect(simpleRowProcedureData)
                            .thenReturn(simpleRowProcedureData);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> {
                            assertEquals("RESEARCH", result.getDeptName());
                            assertEquals("DALLAS", result.getLocation());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callOutputAndMultipleRowProcedureUsingSelect() {
        super.<Tuple2<List<Emp>, SimpleRowProcedureData>>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callOutputAndMultipleRowProcedureUsingSelect(simpleRowProcedureData)
                            .collectList()
                            .map(empList -> Tuples.of(empList, simpleRowProcedureData));
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> {
                            assertEquals("RESEARCH", result.getT2().getDeptName());
                            assertEquals("DALLAS", result.getT2().getLocation());
                            assertEquals(5, result.getT1().size());
                            assertEquals(1L, result.getT1().get(0).getEmpNo());
                            assertEquals(4L, result.getT1().get(1).getEmpNo());
                            assertEquals(8L, result.getT1().get(2).getEmpNo());
                            assertEquals(11L, result.getT1().get(3).getEmpNo());
                            assertEquals(13L, result.getT1().get(4).getEmpNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callSingleRowProcedureUsingSelect() {
        super.<Dept>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    SimpleRowProcedureData simpleRowProcedureData = new SimpleRowProcedureData();
                    simpleRowProcedureData.setEmpNo(1L);
                    simpleRowProcedureData.setDeptNo(2L);
                    return procedureMapper.callSingleRowProcedureUsingSelect(simpleRowProcedureData);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(2L, dept.getDeptNo());
                            assertEquals("RESEARCH", dept.getDeptName());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void callMultipleRowProcedureUsingSelect() {
        super.<Emp>newTestRunner()
                .filterDatabases(MariaDBContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(ProcedureMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    ProcedureMapper procedureMapper = reactiveSqlSession.getMapper(ProcedureMapper.class);
                    return procedureMapper.callMultipleRowProcedureUsingSelect(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(emp -> {
                            assertEquals(7L, emp.getEmpNo());
                        })
                        .assertNext(emp -> {
                            assertEquals(9L, emp.getEmpNo());
                        })
                        .assertNext(emp -> {
                            assertEquals(14L, emp.getEmpNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

}

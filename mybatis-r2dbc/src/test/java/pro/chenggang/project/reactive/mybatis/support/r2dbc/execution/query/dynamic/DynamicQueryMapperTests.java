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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.dynamic;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void countAllDept() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    dynamicQueryMapper.countAllDept()
                            .as(StepVerifier::create)
                            .assertNext(result -> assertEquals(result,4))
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectByDeptNo() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    dynamicQueryMapper.selectByDeptNo(1L)
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(), 1L);
                            })
                            .verifyComplete();
                }
        );
    }

    @Test
    void selectAllDept() {
        runAllDatabases(
                r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                },
                (type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    dynamicQueryMapper.selectAllDept()
                            .as(StepVerifier::create)
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(), 1L);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(), 2L);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(), 3L);
                            })
                            .assertNext(dept -> {
                                assertEquals(dept.getDeptNo(), 4L);
                            })
                            .verifyComplete();
                }
        );
    }

}

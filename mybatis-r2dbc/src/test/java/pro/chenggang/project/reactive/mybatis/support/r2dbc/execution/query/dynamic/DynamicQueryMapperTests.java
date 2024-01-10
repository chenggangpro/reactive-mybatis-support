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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.dynamic;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicQueryMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void countAllDept() {
        super.<Long>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    return dynamicQueryMapper.countAllDept();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(result -> assertEquals(4, result))
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByDeptNo() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    return dynamicQueryMapper.selectByDeptNo(1L);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllDept() {
        super.<Dept>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DynamicQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    DynamicQueryMapper dynamicQueryMapper = reactiveSqlSession.getMapper(DynamicQueryMapper.class);
                    return dynamicQueryMapper.selectAllDept();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(dept -> {
                            assertEquals(1L, dept.getDeptNo());
                        })
                        .assertNext(dept -> {
                            assertEquals(2L, dept.getDeptNo());
                        })
                        .assertNext(dept -> {
                            assertEquals(3L, dept.getDeptNo());
                        })
                        .assertNext(dept -> {
                            assertEquals(4L, dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

}

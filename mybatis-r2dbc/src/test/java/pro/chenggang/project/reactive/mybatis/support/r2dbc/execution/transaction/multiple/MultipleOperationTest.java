/*
 *    Copyright 2009-2025 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.multiple;

import io.r2dbc.spi.IsolationLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.query.simple.SimpleQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.update.UpdateMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
class MultipleOperationTest extends MybatisR2dbcBaseTests {
    
    @Test
    void multipleOperationWithinTransaction() {
        super.<Integer>newTestRunner()
                /*
                 * 1. this test doesn't work with r2dbc-mssql driver
                 *  r2dbc-mssql 0.9.0 has an issue fixed in 1.0.2.RELEASE but the r2dbc-spi's baseline is 1.0.0.RELEASE
                 * issue link: https://github.com/r2dbc/r2dbc-mssql/issues/271
                 * 2. oracle jdbc doesn't support IsolationLevel.READ_UNCOMMITTED
                 * 3. postgresql server treat READ_UNCOMMITTED as READ_COMMITTED
                 */
                .filterDatabases(databaseType -> MySQLContainer.class.equals(databaseType)
                        || MariaDBContainer.class.equals(databaseType)
                )
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSessionOperator) -> {
                    reactiveSqlSessionOperator.executeMonoThenClose(
                                    ReactiveSqlSessionProfile.builder()
                                            .withIsolationLevel(IsolationLevel.READ_COMMITTED)
                                            .forceToRollback()
                                            .build(),
                                    reactiveSqlSession -> {
                                        UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                                        SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                                        Dept dept = new Dept();
                                        dept.setDeptNo(1L);
                                        dept.setDeptName("INSET_DEPT_NAME1");
                                        dept.setLocation("INSET_DEPT_LOCATION");
                                        dept.setCreateTime(LocalDateTime.now());
                                        return updateMapper.updateDeptByDeptNo(dept)
                                                .then(simpleQueryMapper.selectByDeptNo(1L))
                                                .then(Mono.defer(() -> {
                                                    dept.setDeptName("INSET_DEPT_NAME2");
                                                    return updateMapper.updateDeptByDeptNo(dept);
                                                }))
                                                .then(simpleQueryMapper.selectByDeptNo(1L));
                                    }
                            )
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptName(),
                                        "INSET_DEPT_NAME2"
                                );
                            })
                            .verifyComplete();
                })
                .run();
    }

    @Test
    void multipleOperationWithoutTransaction() {
        super.<Integer>newTestRunner()
                /*
                 * 1. this test doesn't work with r2dbc-mssql driver
                 *  r2dbc-mssql 0.9.0 has an issue fixed in 1.0.2.RELEASE but the r2dbc-spi's baseline is 1.0.0.RELEASE
                 * issue link: https://github.com/r2dbc/r2dbc-mssql/issues/271
                 * 2. oracle jdbc doesn't support IsolationLevel.READ_UNCOMMITTED
                 * 3. postgresql server treat READ_UNCOMMITTED as READ_COMMITTED
                 */
                .filterDatabases(databaseType -> MySQLContainer.class.equals(databaseType)
                        || MariaDBContainer.class.equals(databaseType)
                )
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                    r2dbcMybatisConfiguration.addMapper(SimpleQueryMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSessionOperator) -> {
                    reactiveSqlSessionOperator.executeMonoThenClose(
                                    ReactiveSqlSessionProfile.DEFAULT_PROFILE,
                                    reactiveSqlSession -> {
                                        UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                                        SimpleQueryMapper simpleQueryMapper = reactiveSqlSession.getMapper(SimpleQueryMapper.class);
                                        Dept dept = new Dept();
                                        dept.setDeptNo(1L);
                                        dept.setDeptName("INSET_DEPT_NAME1");
                                        dept.setLocation("INSET_DEPT_LOCATION");
                                        dept.setCreateTime(LocalDateTime.now());
                                        return updateMapper.updateDeptByDeptNo(dept)
                                                .then(simpleQueryMapper.selectByDeptNo(1L))
                                                .then(Mono.defer(() -> {
                                                    dept.setDeptName("INSET_DEPT_NAME2");
                                                    return updateMapper.updateDeptByDeptNo(dept);
                                                }))
                                                .then(simpleQueryMapper.selectByDeptNo(1L));
                                    }
                            )
                            .as(StepVerifier::create)
                            .consumeNextWith(dept -> {
                                Assertions.assertEquals(dept.getDeptName(),
                                        "INSET_DEPT_NAME2"
                                );
                            })
                            .verifyComplete();
                })
                .run();
    }
}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.insert;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectContent;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
class InsertMapperTest extends MybatisR2dbcBaseTests {

    Dept dept;

    void resetDept() {
        dept = new Dept();
        dept.setDeptName("INSET_DEPT_NAME");
        dept.setLocation("INSET_DEPT_LOCATION");
        dept.setCreateTime(LocalDateTime.now());
    }

    @Test
    void insertOneDept() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDept(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertNull(dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKey() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKey(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKeyUsingSelectKeyMysql() {
        super.<Integer>newTestRunner()
                .filterDatabases(
                        databaseType -> MySQLContainer.class.equals(databaseType) || MariaDBContainer.class.equals(
                                databaseType))
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKeyUsingSelectKeyMysql(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKeyUsingSelectKeyPostgresql() {
        super.<Integer>newTestRunner()
                .filterDatabases(PostgreSQLContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKeyUsingSelectKeyPostgresql(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithAnnotation(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertNull(dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKeyAndAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKeyAndAnnotation(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKeyAndAnnotationMysql() {
        super.<Integer>newTestRunner()
                .filterDatabases(
                        databaseType -> MySQLContainer.class.equals(databaseType) || MariaDBContainer.class.equals(
                                databaseType))
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKeyAndAnnotationMysql(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertOneDeptWithGeneratedKeyAndAnnotationPostgresql() {
        super.<Integer>newTestRunner()
                .filterDatabases(PostgreSQLContainer.class::equals)
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertOneDeptWithGeneratedKeyAndAnnotationPostgresql(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertMultipleDept() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    List<Dept> deptList = new ArrayList<>();
                    deptList.add(dept);
                    deptList.add(dept);
                    deptList.add(dept);
                    return insertMapper.insertMultipleDept(deptList);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 3);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertMultipleDeptWithAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    List<Dept> deptList = new ArrayList<>();
                    deptList.add(dept);
                    deptList.add(dept);
                    deptList.add(dept);
                    return insertMapper.insertMultipleDeptWithAnnotation(deptList);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 3);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertWithDynamic(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertNull(dept.getDeptNo());
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertAndGeneratedKeyWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    return insertMapper.insertAndGeneratedKeyWithDynamic(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                            assertEquals(dept.getDeptNo(), 5L);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertMultipleWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    this.resetDept();
                    List<Dept> deptList = new ArrayList<>();
                    deptList.add(dept);
                    deptList.add(dept);
                    deptList.add(dept);
                    return insertMapper.insertMultipleWithDynamic(deptList);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 3);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void insertWithBlobAndClod() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(InsertMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    InsertMapper insertMapper = reactiveSqlSession.getMapper(InsertMapper.class);
                    SubjectContent subjectContent = new SubjectContent();
                    subjectContent.setId(3);
                    subjectContent.setBlobContent(Blob.from(
                            Mono.just(ByteBuffer.wrap("This is a test blob content".getBytes(StandardCharsets.UTF_8)))
                    ));
                    subjectContent.setClobContent(
                            Clob.from(Mono.just("This is a test clob content"))
                    );
                    return insertMapper.insertWithBlobAndClod(subjectContent);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

}
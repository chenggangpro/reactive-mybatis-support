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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.update;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectContent;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
class UpdateMapperTest extends MybatisR2dbcBaseTests {

    @Test
    void updateDeptByDeptNo() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                    Dept dept = new Dept();
                    dept.setDeptNo(1L);
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return updateMapper.updateDeptByDeptNo(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void updateDeptByDeptNoWithAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                    Dept dept = new Dept();
                    dept.setDeptNo(1L);
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return updateMapper.updateDeptByDeptNoWithAnnotation(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void updateDeptByDeptNoWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                    Dept dept = new Dept();
                    dept.setDeptNo(1L);
                    dept.setDeptName("INSET_DEPT_NAME");
                    dept.setLocation("INSET_DEPT_LOCATION");
                    dept.setCreateTime(LocalDateTime.now());
                    return updateMapper.updateDeptByDeptNoWithDynamic(dept);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(effectRowCount -> {
                            assertEquals(effectRowCount, 1);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void updateBlobAndClodById() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(UpdateMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    UpdateMapper updateMapper = reactiveSqlSession.getMapper(UpdateMapper.class);
                    SubjectContent subjectContent = new SubjectContent();
                    subjectContent.setId(1);
                    subjectContent.setBlobContent(Blob.from(
                            Mono.just(ByteBuffer.wrap("This is a test blob content".getBytes(StandardCharsets.UTF_8)))
                    ));
                    subjectContent.setClobContent(
                            Clob.from(Mono.just("This is a test clob content"))
                    );
                    return updateMapper.updateBlobAndClodById(subjectContent);
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
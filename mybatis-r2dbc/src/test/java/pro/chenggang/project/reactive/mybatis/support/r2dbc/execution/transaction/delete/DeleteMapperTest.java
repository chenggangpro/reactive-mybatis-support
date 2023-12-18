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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.transaction.delete;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
class DeleteMapperTest extends MybatisR2dbcBaseTests {

    @Test
    void deleteByDeptNo() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNo(4L);
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
    void deleteByDeptNoWithAnnotation() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNoWithAnnotation(4L);
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
    void deleteByDeptNoWithDynamic() {
        super.<Integer>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(DeleteMapper.class);
                })
                .runWithThenRollback((type, reactiveSqlSession) -> {
                    DeleteMapper deleteMapper = reactiveSqlSession.getMapper(DeleteMapper.class);
                    return deleteMapper.deleteByDeptNoWithDynamic(4L);
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
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.setup.MybatisR2dbcBaseTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gang Cheng
 * @since 1.0.0
 */
public class R2dbcStatementLogCacheTests extends MybatisR2dbcBaseTests {

    @Test
    public void testR2dbcStatementLogFactory() {
        R2dbcStatementLogFactory r2dbcStatementLogFactory = super.r2dbcMybatisConfiguration.getR2dbcStatementLogFactory();
        assertThat(r2dbcStatementLogFactory.getAllR2dbcStatementLog()).isNotEmpty();
    }

    @Test
    public void testR2dbcStatementLog() {
        super.r2dbcMybatisConfiguration.getMappedStatements()
                .forEach(mappedStatement -> {
                    R2dbcStatementLog r2dbcStatementLog = super.r2dbcMybatisConfiguration.getR2dbcStatementLog(mappedStatement);
                    assertThat(r2dbcStatementLog).isNotNull();
                });
    }
}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.simple.SimpleQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.DatabaseInitialization.DatabaseConfig;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@ActiveProfiles("xml-config")
@TestInstance(PER_CLASS)
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class MybatisR2dbcXmlConfigApplicationTests extends MybatisR2dbcBaseTests {

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        setUp(MySQLContainer.class, false);
        DatabaseConfig databaseConfig = r2dbcProtocol.getDatabaseConfig();
        registry.add("spring.r2dbc.mybatis.r2dbc-url", r2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.password", databaseConfig::getPassword);
        registry.add("spring.r2dbc.mybatis.username", databaseConfig::getUsername);
        registry.add("r2dbc.mybatis.environment", () -> "mysql");
        registry.add("r2dbc.mybatis.config-location", () -> "classpath:MybatisR2dbcConfig.xml");
    }

    @Autowired
    SimpleQueryMapper simpleQueryMapper;

    @Test
    void testContext() {
        simpleQueryMapper.countAllDept()
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }
}


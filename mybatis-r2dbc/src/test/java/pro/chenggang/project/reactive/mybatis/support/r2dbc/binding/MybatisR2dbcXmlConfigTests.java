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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization.DatabaseConfig;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization.R2dbcProtocol;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.MysqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.PostgresqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcXMLConfigBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.connection.DefaultTransactionSupportConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.type.adapter.AdapterMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcEnvironment;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
@Testcontainers
public class MybatisR2dbcXmlConfigTests {

    protected static final Map<Class<?>, DatabaseInitialization> databaseInitializationContainer;

    static {
        databaseInitializationContainer = new LinkedHashMap<>();
        databaseInitializationContainer.put(MySQLContainer.class, new MysqlTestContainerInitialization());
        databaseInitializationContainer.put(PostgreSQLContainer.class, new PostgresqlTestContainerInitialization());
    }

    protected static final String DB_NAME = "mybatis_r2dbc_test";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";

    protected R2dbcProtocol setUp(Class<?> testContainerClass, boolean dryRun) {
        Hooks.onOperatorDebug();
        Hooks.enableContextLossTracking();
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .databaseName(DB_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        return databaseInitialization.startup(databaseConfig, dryRun);
    }

    @Test
    void loadMysqlConfigurationFromConfigXml() throws Exception {
        R2dbcProtocol r2dbcProtocol = setUp(MySQLContainer.class, false);
        DatabaseConfig databaseConfig = r2dbcProtocol.getDatabaseConfig();
        Properties setupProperties = new Properties();
        setupProperties.setProperty("mysql.driver", "mysql");
        setupProperties.setProperty("mysql.host", r2dbcProtocol.getHost());
        setupProperties.setProperty("mysql.port", String.valueOf(r2dbcProtocol.getPort()));
        setupProperties.setProperty("mysql.database", databaseConfig.getDatabaseName());
        setupProperties.setProperty("mysql.user", databaseConfig.getUsername());
        setupProperties.setProperty("mysql.password", databaseConfig.getPassword());
        try (InputStream inputStream = Resources.getResourceAsStream("MybatisR2dbcConfig.xml")) {
            R2dbcXMLConfigBuilder r2dbcXMLConfigBuilder = new R2dbcXMLConfigBuilder(inputStream,
                    "mysql",
                    setupProperties
            );
            R2dbcMybatisConfiguration r2dbcMybatisConfiguration = r2dbcXMLConfigBuilder.parse();
            assertNotNull(r2dbcMybatisConfiguration);
            R2dbcEnvironment r2dbcEnvironment = r2dbcMybatisConfiguration.getR2dbcEnvironment();
            assertNotNull(r2dbcEnvironment);
            assertNotNull(r2dbcEnvironment.getConnectionFactory());
            assertInstanceOf(ConnectionPool.class, r2dbcEnvironment.getConnectionFactory());
            assertEquals("mysql", r2dbcMybatisConfiguration.getDatabaseId());
        }
    }

    @Test
    void loadPostgresqlConfigurationFromConfigXml() throws Exception {
        R2dbcProtocol r2dbcProtocol = setUp(PostgreSQLContainer.class, false);
        DatabaseConfig databaseConfig = r2dbcProtocol.getDatabaseConfig();
        Properties setupProperties = new Properties();
        setupProperties.setProperty("postgresql.driver", "postgresql");
        setupProperties.setProperty("postgresql.host", r2dbcProtocol.getHost());
        setupProperties.setProperty("postgresql.port", String.valueOf(r2dbcProtocol.getPort()));
        setupProperties.setProperty("postgresql.database", databaseConfig.getDatabaseName());
        setupProperties.setProperty("postgresql.user", databaseConfig.getUsername());
        setupProperties.setProperty("postgresql.password", databaseConfig.getPassword());
        try (InputStream inputStream = Resources.getResourceAsStream("MybatisR2dbcConfig.xml")) {
            R2dbcXMLConfigBuilder r2dbcXMLConfigBuilder = new R2dbcXMLConfigBuilder(inputStream,
                    "postgresql",
                    setupProperties
            );
            R2dbcMybatisConfiguration r2dbcMybatisConfiguration = r2dbcXMLConfigBuilder.parse();
            assertNotNull(r2dbcMybatisConfiguration);
            R2dbcEnvironment r2dbcEnvironment = r2dbcMybatisConfiguration.getR2dbcEnvironment();
            assertNotNull(r2dbcEnvironment);
            assertNotNull(r2dbcEnvironment.getConnectionFactory());
            assertInstanceOf(DefaultTransactionSupportConnectionFactory.class, r2dbcEnvironment.getConnectionFactory());
            assertInstanceOf(PostgresqlConnectionFactory.class, ((DefaultTransactionSupportConnectionFactory) r2dbcEnvironment.getConnectionFactory()).unwrap());
            assertEquals("postgresql", r2dbcMybatisConfiguration.getDatabaseId());
        }
    }

    @Test
    void testForceToUseR2dbcTypeHandlerAdapterConfiguredInXml() throws Exception {
        R2dbcProtocol r2dbcProtocol = setUp(MySQLContainer.class, false);
        DatabaseConfig databaseConfig = r2dbcProtocol.getDatabaseConfig();
        Properties setupProperties = new Properties();
        setupProperties.setProperty("mysql.driver", "mysql");
        setupProperties.setProperty("mysql.host", r2dbcProtocol.getHost());
        setupProperties.setProperty("mysql.port", String.valueOf(r2dbcProtocol.getPort()));
        setupProperties.setProperty("mysql.database", databaseConfig.getDatabaseName());
        setupProperties.setProperty("mysql.user", databaseConfig.getUsername());
        setupProperties.setProperty("mysql.password", databaseConfig.getPassword());
        try (InputStream inputStream = Resources.getResourceAsStream("MybatisR2dbcConfig.xml")) {
            R2dbcXMLConfigBuilder r2dbcXMLConfigBuilder = new R2dbcXMLConfigBuilder(inputStream,
                    "mysql",
                    setupProperties
            );
            R2dbcMybatisConfiguration r2dbcMybatisConfiguration = r2dbcXMLConfigBuilder.parse();
            assertNotNull(r2dbcMybatisConfiguration);
            R2dbcEnvironment r2dbcEnvironment = r2dbcMybatisConfiguration.getR2dbcEnvironment();
            assertNotNull(r2dbcEnvironment);
            assertNotNull(r2dbcEnvironment.getConnectionFactory());
            assertEquals("mysql", r2dbcMybatisConfiguration.getDatabaseId());
            assertInstanceOf(ConnectionPool.class, r2dbcEnvironment.getConnectionFactory());
            r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
            ReactiveSqlSessionFactory reactiveSqlSessionFactory = DefaultReactiveSqlSessionFactory.newBuilder()
                    .withR2dbcMybatisConfiguration(r2dbcMybatisConfiguration)
                    .build();
            AdapterMapper adapterMapper = reactiveSqlSessionFactory.openSession().getMapper(AdapterMapper.class);
            adapterMapper.selectAll()
                    .as(StepVerifier::create)
                    .consumeNextWith(subjectContent -> {
                        Assertions.assertEquals(1, subjectContent.getId());
                        Mono.from(subjectContent.getBlobContent()
                                        .stream())
                                .as(StepVerifier::create)
                                .expectNext(ByteBuffer.wrap("This is a blob content1".getBytes(StandardCharsets.UTF_8)))
                                .verifyComplete();
                        Mono.from(subjectContent.getClobContent()
                                        .stream())
                                .as(StepVerifier::create)
                                .expectNext("This is a clob content1")
                                .verifyComplete();
                    })
                    .consumeNextWith(subjectContent -> {
                        Assertions.assertEquals(2, subjectContent.getId());
                        Mono.from(subjectContent.getBlobContent()
                                        .stream())
                                .as(StepVerifier::create)
                                .expectNext(ByteBuffer.wrap("This is a blob content2".getBytes(StandardCharsets.UTF_8)))
                                .verifyComplete();
                        Mono.from(subjectContent.getClobContent()
                                        .stream())
                                .as(StepVerifier::create)
                                .expectNext("This is a clob content2")
                                .verifyComplete();
                    })
                    .verifyComplete();
        }
    }
}

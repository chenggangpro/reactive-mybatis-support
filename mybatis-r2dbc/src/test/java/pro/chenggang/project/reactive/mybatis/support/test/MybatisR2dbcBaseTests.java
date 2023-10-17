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
package pro.chenggang.project.reactive.mybatis.support.test;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.DatabaseInitialization;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.DatabaseInitialization.DatabaseConfig;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.DatabaseInitialization.R2dbcProtocol;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.MariadbTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.MysqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.test.testcontainers.PostgresqlTestContainerInitialization;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.r2dbc.pool.ConnectionPoolConfiguration.NO_TIMEOUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * Mybatis r2dbc base tests
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@TestInstance(PER_CLASS)
@Testcontainers
public class MybatisR2dbcBaseTests {

    private static final Map<Class<?>, DatabaseInitialization> databaseInitializationContainer;

    static {
        databaseInitializationContainer = new HashMap<>();
        databaseInitializationContainer.put(MySQLContainer.class, new MysqlTestContainerInitialization());
        databaseInitializationContainer.put(MariaDBContainer.class, new MariadbTestContainerInitialization());
        databaseInitializationContainer.put(PostgreSQLContainer.class, new PostgresqlTestContainerInitialization());
    }

    protected static final String DB_NAME = "mybatis_r2dbc_test";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";

    protected R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
    protected ConnectionFactory connectionFactory;
    protected ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    protected void setUp(Class<? extends GenericContainer<?>> testContainerClass) {
        Hooks.onOperatorDebug();
        Hooks.enableContextLossTracking();
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .databaseName(DB_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        R2dbcProtocol r2dbcProtocol = databaseInitialization.startup(databaseConfig);
        log.info("Start up test container success : {}", r2dbcProtocol);
    }

    protected void setUp(Class<? extends GenericContainer<?>> testContainerClass,
                         Function<R2dbcProtocol, R2dbcMybatisConfiguration> r2dbcMybatisConfigurationProvider) {
        Hooks.onOperatorDebug();
        Hooks.enableContextLossTracking();
        Assertions.assertNotNull(testContainerClass);
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .databaseName(DB_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        R2dbcProtocol r2dbcProtocol = databaseInitialization.startup(databaseConfig);
        log.info("Start up test container success : {}", r2dbcProtocol);
        this.connectionFactory = this.connectionFactory(r2dbcProtocol.getProtocolUrlWithCredential());
        this.reactiveSqlSessionFactory = this.reactiveSqlSessionFactory(
                r2dbcMybatisConfigurationProvider.apply(r2dbcProtocol),
                this.connectionFactory
        );
        assertThat(this.r2dbcMybatisConfiguration, notNullValue());
        assertThat(this.connectionFactory, notNullValue());
        assertThat(this.reactiveSqlSessionFactory, notNullValue());
        log.info("Initialize ConnectionFactory ReactiveSqlSessionFactory success");
    }

    protected ConnectionPool connectionFactory(String r2dbcProtocolUrl) {
        ConnectionFactory connectionFactory = ConnectionFactories.get(r2dbcProtocolUrl);
        if (connectionFactory instanceof ConnectionPool) {
            return (ConnectionPool) connectionFactory;
        }
        ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
                .name(DB_NAME)
                .maxSize(Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE)
                .initialSize(Schedulers.DEFAULT_POOL_SIZE)
                .maxIdleTime(Duration.ofMinutes(1))
                .acquireRetry(1)
                .backgroundEvictionInterval(NO_TIMEOUT)
                .maxAcquireTime(NO_TIMEOUT)
                .maxCreateConnectionTime(NO_TIMEOUT)
                .maxLifeTime(NO_TIMEOUT)
                .validationDepth(ValidationDepth.REMOTE)
                .validationQuery("SELECT 1");
        return new ConnectionPool(builder.build());
    }

    protected ReactiveSqlSessionFactory reactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration,
                                                                  ConnectionFactory connectionFactory) {
        return DefaultReactiveSqlSessionFactory.newBuilder()
                .withConnectionFactory(connectionFactory)
                .withR2dbcMybatisConfiguration(configuration)
                .build();
    }

}

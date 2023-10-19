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
package pro.chenggang.project.reactive.mybatis.support;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization.DatabaseConfig;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.DatabaseInitialization.R2dbcProtocol;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.MariadbTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.MysqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.PostgresqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcXMLMapperBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.r2dbc.pool.ConnectionPoolConfiguration.NO_TIMEOUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Mybatis r2dbc base tests
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Testcontainers
public class MybatisR2dbcBaseTests {

    protected static final Map<Class<?>, DatabaseInitialization> databaseInitializationContainer;
    protected static final List<String> commonXmlMapperLocations = new ArrayList<>();

    static {
        databaseInitializationContainer = new HashMap<>();
        databaseInitializationContainer.put(MySQLContainer.class, new MysqlTestContainerInitialization());
        databaseInitializationContainer.put(MariaDBContainer.class, new MariadbTestContainerInitialization());
        databaseInitializationContainer.put(PostgreSQLContainer.class, new PostgresqlTestContainerInitialization());
    }

    static {
        commonXmlMapperLocations.add("pro/chenggang/project/reactive/mybatis/support/common/DeptMapper.xml");
        commonXmlMapperLocations.add("pro/chenggang/project/reactive/mybatis/support/common/EmpMapper.xml");
        commonXmlMapperLocations.add("pro/chenggang/project/reactive/mybatis/support/common/SubjectMapper.xml");
        commonXmlMapperLocations.add("pro/chenggang/project/reactive/mybatis/support/common/SubjectDataMapper.xml");
    }

    protected static final String DB_NAME = "mybatis_r2dbc_test";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";

    private ConnectionFactory connectionFactory;
    private ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    protected void setUp(Class<?> testContainerClass, boolean dryRun) {
        Hooks.onOperatorDebug();
        Hooks.enableContextLossTracking();
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .databaseName(DB_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        databaseInitialization.startup(databaseConfig, dryRun);
    }

    protected ReactiveSqlSessionFactory setUp(Class<?> testContainerClass,
                                              boolean dryRun,
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
        R2dbcProtocol r2dbcProtocol = databaseInitialization.startup(databaseConfig, dryRun);
        this.connectionFactory = this.connectionFactory(r2dbcProtocol.getProtocolUrlWithCredential());
        this.reactiveSqlSessionFactory = this.reactiveSqlSessionFactory(
                r2dbcMybatisConfigurationProvider.apply(r2dbcProtocol),
                this.connectionFactory
        );
        assertThat(this.connectionFactory, notNullValue());
        assertThat(this.reactiveSqlSessionFactory, notNullValue());
        log.info("Initialize ConnectionFactory ReactiveSqlSessionFactory success");
        return this.reactiveSqlSessionFactory;
    }

    protected void destroy(Class<?> testContainerClass, boolean dryRun) {
        if (dryRun) {
            return;
        }
        try {
            reactiveSqlSessionFactory.close();
        } catch (Exception e) {
            // ignore
        }
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        databaseInitialization.destroy();
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

    protected void loadXmlMapper(String xmlMapperLocation, R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        try (InputStream inputStream = Resources.getResourceAsStream(
                xmlMapperLocation)) {
            R2dbcXMLMapperBuilder r2dbcXMLMapperBuilder = new R2dbcXMLMapperBuilder(inputStream,
                    r2dbcMybatisConfiguration,
                    xmlMapperLocation,
                    r2dbcMybatisConfiguration.getSqlFragments()
            );
            r2dbcXMLMapperBuilder.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void runAllDatabases(Consumer<R2dbcMybatisConfiguration> r2dbcMybatisConfigurationInitialization,
                                   BiConsumer<Class<?>, ReactiveSqlSession> reactiveSqlSessionConsumer) {
        for (Class<?> aClass : MybatisR2dbcBaseTests.databaseInitializationContainer.keySet()) {
            log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", aClass.getSimpleName());
            ReactiveSqlSessionFactory reactiveSqlSessionFactory = setUp(aClass, false, r2dbcProtocol -> {
                R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
                for (String commonXmlMapperLocation : commonXmlMapperLocations) {
                    loadXmlMapper(commonXmlMapperLocation, r2dbcMybatisConfiguration);
                }
                r2dbcMybatisConfigurationInitialization.accept(r2dbcMybatisConfiguration);
                return r2dbcMybatisConfiguration;
            });
            ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionFactory.openSession();
            reactiveSqlSessionConsumer.accept(aClass, reactiveSqlSession);
            destroy(aClass, false);
            log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", aClass.getSimpleName());
        }
    }

    protected void dryRunAllDatabases(Consumer<R2dbcMybatisConfiguration> r2dbcMybatisConfigurationInitialization,
                                      BiConsumer<Class<?>, ReactiveSqlSession> reactiveSqlSessionConsumer) {
        for (Class<?> aClass : MybatisR2dbcBaseTests.databaseInitializationContainer.keySet()) {
            log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", aClass.getSimpleName());
            ReactiveSqlSessionFactory reactiveSqlSessionFactory = setUp(aClass, true, r2dbcProtocol -> {
                R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
                r2dbcMybatisConfigurationInitialization.accept(r2dbcMybatisConfiguration);
                return r2dbcMybatisConfiguration;
            });
            ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionFactory.openSession();
            reactiveSqlSessionConsumer.accept(aClass, reactiveSqlSession);
            destroy(aClass, true);
            log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", aClass.getSimpleName());
        }
    }

    @Test
    void validateTestcontainers() {
        for (Class<?> aClass : MybatisR2dbcBaseTests.databaseInitializationContainer.keySet()) {
            log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", aClass.getSimpleName());
            setUp(aClass, false, r2dbcProtocol -> new R2dbcMybatisConfiguration());
            destroy(aClass, false);
            log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", aClass.getSimpleName());
        }
    }
}

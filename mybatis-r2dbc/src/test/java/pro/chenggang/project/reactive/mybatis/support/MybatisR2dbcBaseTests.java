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
import org.reactivestreams.Publisher;
import org.testcontainers.containers.MSSQLServerContainer;
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
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.SqlServerTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionOperator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcXMLMapperBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionOperator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    private static final AtomicBoolean validateTestcontainersFlag = new AtomicBoolean(false);

    static {
        databaseInitializationContainer = new LinkedHashMap<>();
        databaseInitializationContainer.put(MySQLContainer.class, new MysqlTestContainerInitialization());
        databaseInitializationContainer.put(MariaDBContainer.class, new MariadbTestContainerInitialization());
        databaseInitializationContainer.put(PostgreSQLContainer.class, new PostgresqlTestContainerInitialization());
        databaseInitializationContainer.put(MSSQLServerContainer.class, new SqlServerTestContainerInitialization());
        // Oracle R2DBC can only interoperate with libraries that support the 1.0.0.RELEASE version of the R2DBC SPI.
//        databaseInitializationContainer.put(OracleContainer.class,new OracleTestContainerInitialization());
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

    protected <T> MybatisR2dbcTestRunner<T> newTestRunner() {
        return new MybatisR2dbcTestRunner<>();
    }

    @Test
    void validateTestcontainers() {
        if (!validateTestcontainersFlag.compareAndSet(false, true)) {
            log.info("All testcontainers have already been validated.");
            return;
        }
        for (Class<?> aClass : MybatisR2dbcBaseTests.databaseInitializationContainer.keySet()) {
            log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", aClass.getSimpleName());
            setUp(aClass, false, r2dbcProtocol -> new R2dbcMybatisConfiguration());
            destroy(aClass, false);
            log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", aClass.getSimpleName());
        }
    }

    protected class MybatisR2dbcTestRunner<T> {

        private boolean dryRun;
        private Predicate<Class<?>> databaseFilter = __ -> true;
        private Set<String> xmlMapperLocations = new HashSet<>();
        private Consumer<R2dbcMybatisConfiguration> r2dbcMybatisConfigurationCustomizer;
        private BiConsumer<Class<?>, ReactiveSqlSessionFactory> reactiveSqlSessionFactoryTestRunner;
        private BiFunction<Class<?>, ReactiveSqlSession, ? extends Publisher<T>> reactiveSqlSessionTestRunner;
        private Function<StepVerifier.FirstStep<T>, Duration> stepVerifierRunner;
        private BiFunction<Class<?>, ReactiveSqlSession, ? extends Mono<T>> reactiveSqlSessionTestRollbackMonoRunner;
        private BiFunction<Class<?>, ReactiveSqlSession, ? extends Flux<T>> reactiveSqlSessionTestRollbackFluxRunner;

        public MybatisR2dbcTestRunner() {
        }

        public MybatisR2dbcTestRunner<T> dryRun() {
            this.dryRun = true;
            return this;
        }

        public MybatisR2dbcTestRunner<T> allDatabases() {
            this.databaseFilter = aClass -> true;
            return this;
        }

        public MybatisR2dbcTestRunner<T> filterDatabases(Predicate<Class<?>> databaseFilter) {
            this.databaseFilter = databaseFilter;
            return this;
        }

        public MybatisR2dbcTestRunner<T> addXmlMapperLocation(String xmlMapperLocation) {
            this.xmlMapperLocations.add(xmlMapperLocation);
            return this;
        }

        public MybatisR2dbcTestRunner<T> customizeR2dbcConfiguration(Consumer<R2dbcMybatisConfiguration> r2dbcMybatisConfigurationCustomizer) {
            this.r2dbcMybatisConfigurationCustomizer = r2dbcMybatisConfigurationCustomizer;
            return this;
        }

        public MybatisR2dbcTestRunner<T> runWith(BiFunction<Class<?>, ReactiveSqlSession, ? extends Publisher<T>> reactiveSqlSessionTestRunner) {
            this.reactiveSqlSessionTestRunner = reactiveSqlSessionTestRunner;
            return this;
        }

        public MybatisR2dbcTestRunner<T> runWithThenRollback(BiFunction<Class<?>, ReactiveSqlSession, ? extends Mono<T>> reactiveSqlSessionTestRollbackMonoRunner) {
            this.reactiveSqlSessionTestRollbackMonoRunner = reactiveSqlSessionTestRollbackMonoRunner;
            return this;
        }

        public MybatisR2dbcTestRunner<T> runWithThenRollbackMany(BiFunction<Class<?>, ReactiveSqlSession, ? extends Flux<T>> reactiveSqlSessionTestRollbackFluxRunner) {
            this.reactiveSqlSessionTestRollbackFluxRunner = reactiveSqlSessionTestRollbackFluxRunner;
            return this;
        }

        public MybatisR2dbcTestRunner<T> runWithReactiveSqlSessionFactory(BiConsumer<Class<?>, ReactiveSqlSessionFactory> reactiveSqlSessionFactoryTestRunner) {
            this.reactiveSqlSessionFactoryTestRunner = reactiveSqlSessionFactoryTestRunner;
            return this;
        }

        public MybatisR2dbcTestRunner<T> verifyWith(Function<StepVerifier.FirstStep<T>, Duration> stepVerifierRunner) {
            this.stepVerifierRunner = stepVerifierRunner;
            return this;
        }

        public void run() {
            databaseInitializationContainer.keySet()
                    .stream()
                    .filter(databaseFilter)
                    .forEach(databaseClass -> {
                        log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", databaseClass.getSimpleName());
                        ReactiveSqlSessionFactory reactiveSqlSessionFactory = setUp(databaseClass,
                                dryRun,
                                r2dbcProtocol -> {
                                    R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
                                    r2dbcMybatisConfigurationCustomizer.accept(
                                            r2dbcMybatisConfiguration);
                                    for (String commonXmlMapperLocation : commonXmlMapperLocations) {
                                        loadXmlMapper(
                                                commonXmlMapperLocation,
                                                r2dbcMybatisConfiguration
                                        );
                                    }
                                    for (String xmlMapperLocation : xmlMapperLocations) {
                                        loadXmlMapper(
                                                xmlMapperLocation,
                                                r2dbcMybatisConfiguration
                                        );
                                    }
                                    return r2dbcMybatisConfiguration;
                                }
                        );
                        if (Objects.nonNull(this.reactiveSqlSessionFactoryTestRunner)) {
                            reactiveSqlSessionFactoryTestRunner.accept(
                                    databaseClass,
                                    reactiveSqlSessionFactory
                            );
                        } else if (Objects.nonNull(this.reactiveSqlSessionTestRunner)) {
                            ReactiveSqlSession reactiveSqlSession = reactiveSqlSessionFactory.openSession();
                            stepVerifierRunner.apply(StepVerifier.create(reactiveSqlSessionTestRunner.apply(
                                    databaseClass,
                                    reactiveSqlSession
                            )));
                        } else if (Objects.nonNull(this.reactiveSqlSessionTestRollbackMonoRunner)) {
                            ReactiveSqlSessionOperator reactiveSqlSessionOperator = new DefaultReactiveSqlSessionOperator(
                                    reactiveSqlSessionFactory
                            );
                            stepVerifierRunner.apply(StepVerifier.create(reactiveSqlSessionOperator.executeAndRollback(
                                    reactiveSession -> reactiveSqlSessionTestRollbackMonoRunner.apply(databaseClass,
                                            reactiveSession
                                    ))
                            ));
                        } else if (Objects.nonNull(this.reactiveSqlSessionTestRollbackFluxRunner)) {
                            ReactiveSqlSessionOperator reactiveSqlSessionOperator = new DefaultReactiveSqlSessionOperator(
                                    reactiveSqlSessionFactory
                            );
                            stepVerifierRunner.apply(
                                    StepVerifier.create(reactiveSqlSessionOperator.executeManyAndRollback(
                                            reactiveSession -> reactiveSqlSessionTestRollbackFluxRunner.apply(
                                                    databaseClass,
                                                    reactiveSession
                                            ))
                                    ));
                        } else {
                            log.info("None test runner configured");
                        }
                        destroy(databaseClass, false);
                        log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", databaseClass.getSimpleName());
                    });
        }
    }
}

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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.DatabaseInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.DatabaseInitialization.DatabaseConfig;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.DatabaseInitialization.R2dbcProtocol;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.MariadbTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.MysqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.OracleTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.PostgresqlTestContainerInitialization;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.SqlServerTestContainerInitialization;
import reactor.core.publisher.Hooks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final AtomicBoolean validateTestcontainersFlag = new AtomicBoolean(false);

    static {
        databaseInitializationContainer = new LinkedHashMap<>();
        databaseInitializationContainer.put(MySQLContainer.class, new MysqlTestContainerInitialization());
        databaseInitializationContainer.put(MariaDBContainer.class, new MariadbTestContainerInitialization());
        databaseInitializationContainer.put(PostgreSQLContainer.class, new PostgresqlTestContainerInitialization());
        databaseInitializationContainer.put(MSSQLServerContainer.class, new SqlServerTestContainerInitialization());
        databaseInitializationContainer.put(OracleContainer.class,new OracleTestContainerInitialization());
    }

    protected static final String DB_NAME = "mybatis_r2dbc_test";
    protected static final String USERNAME = "username";
    protected static final String PASSWORD = "password";

    protected static R2dbcProtocol r2dbcProtocol;
    protected static Class<?> currentContainerType;

    protected static R2dbcProtocol setUp(Class<?> testContainerClass, boolean dryRun) {
        Hooks.onOperatorDebug();
        Hooks.enableContextLossTracking();
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        DatabaseConfig databaseConfig = DatabaseConfig.builder()
                .databaseName(DB_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        R2dbcProtocol startedR2dbcProtocol = databaseInitialization.startup(databaseConfig, dryRun);
        currentContainerType = testContainerClass;
        r2dbcProtocol = startedR2dbcProtocol;
        return r2dbcProtocol;
    }

    protected static void destroy(Class<?> testContainerClass, boolean dryRun) {
        if (dryRun) {
            return;
        }
        DatabaseInitialization databaseInitialization = databaseInitializationContainer.get(testContainerClass);
        databaseInitialization.destroy();
    }

    @Disabled
    @Test
    void validateTestcontainers() {
        if (!validateTestcontainersFlag.compareAndSet(false, true)) {
            log.info("All testcontainers have already been validated.");
            return;
        }
        String envDatabaseType = System.getProperty("databaseType",
                MariaDBContainer.class.getSimpleName()
        );
        for (Class<?> aClass : MybatisR2dbcBaseTests.databaseInitializationContainer.keySet()) {
            if(!aClass.getSimpleName().equalsIgnoreCase(envDatabaseType)){
                continue;
            }
            log.info("⬇⬇⬇⬇⬇⬇ {} ----------------", aClass.getSimpleName());
            Hooks.onOperatorDebug();
            Hooks.enableContextLossTracking();
            Assertions.assertNotNull(aClass);
            setUp(aClass, false);
            log.info("Start up database success : {}", aClass);
            destroy(aClass, false);
            log.info("⬆⬆⬆⬆⬆⬆ {} ----------------", aClass.getSimpleName());
        }
    }

}

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
package pro.chenggang.project.reactive.mybatis.support.common.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.testcontainers.containers.MSSQLServerContainer.DEFAULT_TAG;
import static org.testcontainers.containers.MSSQLServerContainer.IMAGE;
import static org.testcontainers.containers.MSSQLServerContainer.MS_SQL_SERVER_PORT;

/**
 * The postgresql test container initialization
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class SqlServerTestContainerInitialization implements DatabaseInitialization {

    private GenericContainer<?> sqlServerTestContainer;

    @Override
    public boolean supportTestContainer(Class<? extends GenericContainer<?>> testContainerClass) {
        return MSSQLServerContainer.class.equals(testContainerClass);
    }

    @Override
    public R2dbcProtocol startup(DatabaseConfig databaseConfig, boolean dryRun) {
        DatabaseConfig specificDatabaseConfig = databaseConfig.toBuilder()
                .databaseName("master")
                .username("SA")
                .password("R2DBC@password")
                .build();
        if (dryRun) {
            R2dbcProtocol r2dbcProtocol = R2dbcProtocol.builder()
                    .databaseConfig(specificDatabaseConfig)
                    .protocolSymbol("mssql")
                    .host("127.0.0.1")
                    .port(MS_SQL_SERVER_PORT)
                    .build();
            log.info("[DryRun] Start up test container success : {}", r2dbcProtocol);
            return r2dbcProtocol;
        }
        JdbcDatabaseContainer<?> jdbcDatabaseContainer = new MSSQLServerContainer<>(DockerImageName.parse(IMAGE)
                .withTag(DEFAULT_TAG))
                .acceptLicense()
                .withStartupCheckStrategy(new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(10)))
                .withPassword(specificDatabaseConfig.getPassword())
                .withUrlParam("encrypt", "false")
                .withInitScript("sql-script/init_mssql.sql");
        sqlServerTestContainer = jdbcDatabaseContainer;
        sqlServerTestContainer.start();
        R2dbcProtocol r2dbcProtocol = R2dbcProtocol.builder()
                .databaseConfig(specificDatabaseConfig)
                .protocolSymbol("mssql")
                .host(jdbcDatabaseContainer.getHost())
                .port(jdbcDatabaseContainer.getMappedPort(MS_SQL_SERVER_PORT))
                .options("encrypt=false")
                .build();
        log.info("Start up test container success : {}", r2dbcProtocol);
        return r2dbcProtocol;
    }

    @Override
    public boolean isRunning() {
        return sqlServerTestContainer != null && sqlServerTestContainer.isRunning();
    }

    @Override
    public void destroy() {
        if (sqlServerTestContainer != null) {
            sqlServerTestContainer.stop();
            log.info("Stop test container success");
        }
    }
}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.utility.DockerImageName;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.support.ScriptRunner;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The postgresql test container initialization
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class OracleTestContainerInitialization implements DatabaseInitialization {

    private GenericContainer<?> oracleTestContainer;

    @Override
    public boolean supportTestContainer(Class<? extends GenericContainer<?>> testContainerClass) {
        return OracleContainer.class.equals(testContainerClass);
    }

    @Override
    public R2dbcProtocol startup(DatabaseConfig databaseConfig, boolean dryRun) {
        DatabaseConfig specificDatabaseConfig = databaseConfig.toBuilder()
                .build();
        if (dryRun) {
            R2dbcProtocol r2dbcProtocol = R2dbcProtocol.builder()
                    .databaseConfig(specificDatabaseConfig)
                    .protocolSymbol("oracle")
                    .host("127.0.0.1")
                    .port(1521)
                    .build();
            log.info("[DryRun] Start up test container success : {}", r2dbcProtocol);
            return r2dbcProtocol;
        }
        JdbcDatabaseContainer<?> jdbcDatabaseContainer = new OracleContainer(DockerImageName.parse(
                "gvenzl/oracle-xe:21-slim-faststart"))
                .withDatabaseName(specificDatabaseConfig.getDatabaseName())
                .withUsername(specificDatabaseConfig.getUsername())
                .withPassword(specificDatabaseConfig.getPassword())
                .withConnectTimeoutSeconds(10);
        oracleTestContainer = jdbcDatabaseContainer;
        oracleTestContainer.start();
        initScriptWithCustomizedScriptRunner("sql-script/init_oracle.sql");
        R2dbcProtocol r2dbcProtocol = R2dbcProtocol.builder()
                .databaseConfig(specificDatabaseConfig)
                .protocolSymbol("oracle")
                .host(jdbcDatabaseContainer.getHost())
                .port(jdbcDatabaseContainer.getMappedPort(1521))
                .build();
        log.info("Start up test container success : {}", r2dbcProtocol);
        return r2dbcProtocol;
    }

    @Override
    public boolean isRunning() {
        return oracleTestContainer != null && oracleTestContainer.isRunning();
    }

    @Override
    public void destroy() {
        if (oracleTestContainer != null) {
            oracleTestContainer.stop();
            log.info("Stop test container success");
        }
    }

    private void initScriptWithCustomizedScriptRunner(String scriptFileName) {
        try (Connection connection = ((JdbcDatabaseContainer<?>) this.oracleTestContainer).createConnection("")) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(scriptFileName);
            if (resource == null) {
                resource = ScriptUtils.class.getClassLoader().getResource(scriptFileName);
                if (resource == null) {
                    log.warn("Could not load classpath init script: {}", scriptFileName);
                    throw new ScriptUtils.ScriptLoadException(
                            "Could not load classpath init script: " + scriptFileName + ". Resource not found."
                    );
                }
            }
            // use ScriptRunner from mybatis instead of the original StringUtils
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(null);
            scriptRunner.setStopOnError(true);
            scriptRunner.runScript(new FileReader(resource.getFile()));
        } catch (IOException e) {
            log.warn("Could not load classpath init script: {}", scriptFileName);
            throw new ScriptUtils.ScriptLoadException("Could not load classpath init script: " + scriptFileName, e);
        } catch (SQLException e) {
            log.warn("Could not execute classpath init script: {}", scriptFileName);
            throw new ScriptUtils.ScriptLoadException("Could not execute classpath init script: " + scriptFileName, e);
        }
    }
}

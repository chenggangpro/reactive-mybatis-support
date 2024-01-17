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
package pro.chenggang.project.reactive.mybatis.support.common.testcontainers;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.ext.ScriptUtils;
import pro.chenggang.project.reactive.mybatis.support.common.testcontainers.support.ScriptRunner;

import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * The Database initialization.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DatabaseInitialization {

    /**
     * Whether support target test container class.
     *
     * @param testContainerClass the test container class
     * @return the true or false
     */
    boolean supportTestContainer(Class<? extends GenericContainer<?>> testContainerClass);

    /**
     * Startup test container.
     *
     * @param databaseConfig the database config
     * @param dryRun         whether dry run
     * @return the r2dbc protocol
     */
    R2dbcProtocol startup(DatabaseConfig databaseConfig, boolean dryRun);

    /**
     * Is test container running.
     *
     * @return the true or false
     */
    boolean isRunning();

    /**
     * Destroy test container.
     */
    void destroy();

    /**
     * Init script with customized script runner.
     *
     * @param jdbcDatabaseContainer the jdbc database container
     * @param scriptFileName        the script file name
     */
    static void initScriptWithCustomizedScriptRunner(JdbcDatabaseContainer<?> jdbcDatabaseContainer,String scriptFileName) {
        try (Connection connection = (jdbcDatabaseContainer.createConnection(""))) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(scriptFileName);
            if (resource == null) {
                resource = ScriptUtils.class.getClassLoader().getResource(scriptFileName);
                if (resource == null) {
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
            throw new ScriptUtils.ScriptLoadException("Could not load classpath init script: " + scriptFileName, e);
        } catch (SQLException e) {
            throw new ScriptUtils.ScriptLoadException("Could not execute classpath init script: " + scriptFileName, e);
        }
    }

    /**
     * The database config.
     */
    @Builder(toBuilder = true)
    @Value(staticConstructor = "of")
    class DatabaseConfig {

        String databaseName;
        String username;
        String password;

    }

    /**
     * The r2dbc protocol.
     */
    @ToString
    @Builder
    @Value(staticConstructor = "of")
    class R2dbcProtocol {

        private static final String PROTOCOL_TEMPLATE = "r2dbc:%s://%s:%s/%s";
        private static final String PROTOCOL_WITH_OPTIONS_TEMPLATE = "r2dbc:%s://%s:%s/%s?%s";

        String protocolSymbol;
        String host;
        int port;
        DatabaseConfig databaseConfig;
        String options;
        String validationQuery;

        /**
         * Gets protocol url.
         *
         * @return the protocol url
         */
        public String getProtocolUrl() {
            if (Objects.nonNull(options) && options.length() > 0) {
                return String.format(PROTOCOL_WITH_OPTIONS_TEMPLATE,
                        protocolSymbol,
                        host,
                        port,
                        databaseConfig.getDatabaseName(),
                        options
                );
            }
            return String.format(PROTOCOL_TEMPLATE,
                    protocolSymbol,
                    host,
                    port,
                    databaseConfig.getDatabaseName()
            );
        }

        /**
         * Gets protocol url with credential.
         *
         * @return the protocol url with credential
         */
        public String getProtocolUrlWithCredential() {
            String databaseConfigUsername = databaseConfig.getUsername();
            String databaseConfigPassword = databaseConfig.getPassword();
            String encodedUsername;
            try {
                encodedUsername = URLEncoder.encode(databaseConfigUsername, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                //fallback to original username
                encodedUsername = databaseConfigUsername;
            }
            String encodedPassword;
            try {
                encodedPassword = URLEncoder.encode(databaseConfigPassword, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                //fallback to original password
                encodedPassword = databaseConfigPassword;
            }
            String credential = encodedUsername + (databaseConfigPassword.isEmpty() ? "" : ":" + encodedPassword);
            return this.getProtocolUrl().replace("//", "//" + credential + "@");
        }
    }
}

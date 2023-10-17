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
package pro.chenggang.project.reactive.mybatis.support.test.testcontainers;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.testcontainers.containers.GenericContainer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * @return the r2dbc protocol
     */
    R2dbcProtocol startup(DatabaseConfig databaseConfig);

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
     * The database config.
     */
    @Builder
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

        String protocolSymbol;
        String host;
        int port;
        DatabaseConfig databaseConfig;

        /**
         * Gets protocol url.
         *
         * @return the protocol url
         */
        public String getProtocolUrl() {
            return String.format(protocolSymbol,
                    protocolSymbol,
                    host,
                    protocolSymbol,
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
            return this.getProtocolUrl()
                    .replace("//", "//" + credential + "@");
        }
    }
}

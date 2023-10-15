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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.support;

import io.r2dbc.spi.ValidationDepth;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ClassUtils;
import reactor.util.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static io.r2dbc.pool.ConnectionPoolConfiguration.NO_TIMEOUT;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Gang Cheng
 */
@Getter
@Setter
@ToString
public class R2dbcConnectionFactoryProperties {

    /**
     * Name of the connection factory
     */
    private String name;

    /**
     * Whether to generate a random connection factory name.
     */
    private boolean generateUniqueName = true;

    /**
     * R2dbc format Url
     */
    private String r2dbcUrl;

    /**
     * Login username of the database.
     */
    private String username;

    /**
     * Login password of the database.
     */
    private String password;

    /**
     * r2dbc factory pull
     */
    private Pool pool = new Pool();

    /**
     * r2dbc connection factory metrics enabled
     */
    private Boolean enableMetrics = Boolean.FALSE;

    /**
     * r2dbc connection factory name based on configuration
     *
     * @return the connection factory name to use or {@code null}
     */
    public String determineConnectionFactoryName() {
        if (this.generateUniqueName && !hasText(this.name)) {
            this.name = UUID.randomUUID().toString();
        }
        return this.name;
    }

    /**
     * r2dbc connection url
     *
     * @return
     */
    public String determineConnectionFactoryUrl() {
        if (!hasText(this.r2dbcUrl)) {
            return null;
        }
        String encodedUsername;
        try {
            encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            //fallback to original username
            encodedUsername = username;
        }
        String encodedPassword;
        try {
            encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            //fallback to original password
            encodedPassword = password;
        }
        String credential = encodedUsername + (password == null || password.isEmpty() ? "" : ":" + encodedPassword);
        //only replace 'r2dbc:mysql:' with 'r2dbc:mariadb' when using 'MariadbConnectionFactory'
        boolean isMariadbConnectionfactoryPresent = ClassUtils.isPresent("org.mariadb.r2dbc.MariadbConnectionFactory", this.getClass().getClassLoader());
        if (isMariadbConnectionfactoryPresent && this.r2dbcUrl.startsWith("r2dbc:mysql:")) {
            this.r2dbcUrl = this.r2dbcUrl.replace("r2dbc:mysql:", "r2dbc:mariadb:");
        }
        this.r2dbcUrl = r2dbcUrl.replace("//", "//" + credential + "@");
        return this.r2dbcUrl;
    }

    @Getter
    @Setter
    @ToString
    public static class Pool {

        /**
         * r2dbc connection factory initial size
         * default 1
         */
        private Integer initialSize = 1;

        /**
         * r2dbc connection factory max size
         * default 10
         */
        private Integer maxSize = 10;

        /**
         * r2dbc connection factory max idle time
         */
        private Duration maxIdleTime = Duration.ofMinutes(30);

        /**
         * r2dbc connection factory max create connection time
         * Duration.ZERO indicates immediate failure if the connection is not created immediately.
         */
        private Duration maxCreateConnectionTime = NO_TIMEOUT;

        /**
         * r2dbc connection factory max acquire time
         * Duration.ZERO indicates that the connection must be immediately available otherwise acquisition fails.
         * A negative or a null value results in not applying a timeout.
         */
        private Duration maxAcquireTime = NO_TIMEOUT;

        /**
         * r2dbc connection factory max life time
         * Duration.ZERO indicates immediate connection disposal.
         * A negative or a null value results in not applying a timeout.
         */
        private Duration maxLifeTime = NO_TIMEOUT;

        /**
         * r2dbc connection factory validation query
         */
        @Nullable
        private String validationQuery;

        /**
         * r2dbc connection factory validation depth
         * LOCAL Perform a client-side only validation
         * REMOTE Perform a remote connection validations
         * {@link ValidationDepth}
         */
        private ValidationDepth validationDepth = ValidationDepth.REMOTE;

        /**
         * r2dbc connection factory acquire retry
         * ZERO indicates no-retry
         * default 1
         */
        private int acquireRetry = 1;

        /**
         * r2dbc connection factory background eviction interval
         * ZERO indicates no-timeout, negative marks unconfigured.
         */
        private Duration backgroundEvictionInterval = NO_TIMEOUT;

    }

}

/*
 *    Copyright 2009-2025 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile.DEFAULT_PROFILE;

/**
 * The interface Reactive sql session factory.
 * <p>
 * Factory interface for creating reactive SQL sessions. This factory provides methods to open
 * reactive SQL sessions with different profiles and retrieve the underlying R2DBC MyBatis configuration.
 * Implements AutoCloseable to support resource management and proper cleanup.
 * </p>
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSessionFactory extends AutoCloseable {

    /**
     * Opens a new reactive SQL session with the specified profile.
     * <p>
     * Creates and returns a new ReactiveSqlSession instance configured according to the
     * provided ReactiveSqlSessionProfile. The profile determines session characteristics
     * such as transaction behavior and execution settings.
     * </p>
     *
     * @param reactiveSqlSessionProfile the reactive sql session profile that defines the
     *                                  configuration and behavior of the session to be created
     * @return a new reactive sql session instance configured with the specified profile
     */
    ReactiveSqlSession openSession(ReactiveSqlSessionProfile reactiveSqlSessionProfile);

    /**
     * Opens a new reactive SQL session with the default profile.
     * <p>
     * Convenience method that creates a new ReactiveSqlSession using the default profile
     * configuration. This is equivalent to calling {@link #openSession(ReactiveSqlSessionProfile)}
     * with {@link ReactiveSqlSessionProfile#DEFAULT_PROFILE}.
     * </p>
     *
     * @return a new reactive sql session instance configured with the default profile
     */
    default ReactiveSqlSession openSession() {
        return openSession(DEFAULT_PROFILE);
    }

    /**
     * Retrieves the R2DBC MyBatis configuration associated with this factory.
     * <p>
     * Returns the configuration object that contains all MyBatis settings, mappers,
     * type handlers, and other configuration details specific to R2DBC integration.
     * </p>
     *
     * @return the R2dbcMybatisConfiguration instance containing the factory's configuration
     */
    R2dbcMybatisConfiguration getConfiguration();
}

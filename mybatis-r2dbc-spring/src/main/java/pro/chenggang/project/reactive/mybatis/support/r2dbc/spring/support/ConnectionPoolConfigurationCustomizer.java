/*
 *    Copyright 2009-2026 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import io.r2dbc.pool.ConnectionPoolConfiguration;

import java.util.Optional;

/**
 * ConnectionPoolConfiguration customizer
 *
 * @author Gang Cheng
 * @version 1.0.3
 * @since 1.0.3
 */
@FunctionalInterface
public interface ConnectionPoolConfigurationCustomizer {

    /**
     * The routing name of the connection factory, this is used for dynamic routing.
     * If is empty, the customizer will be applied to all connection factories.
     *
     * @return the routing name or Optional.empty() if the customizer should be applied to all connection factories.
     */
    default Optional<String> routingName() {
        return Optional.empty();
    }

    /**
     * customize ConnectionPoolConfiguration
     *
     * @param connectionPoolConfigurationBuilder the ConnectionPoolConfiguration.Builder
     */
    void customize(ConnectionPoolConfiguration.Builder connectionPoolConfigurationBuilder);
}

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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.DefaultReactiveMybatisExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveMybatisExecutor;

import java.io.Closeable;
import java.util.Objects;

/**
 * The type Default reactive sql session factory.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class DefaultReactiveSqlSessionFactory implements ReactiveSqlSessionFactory {

    private final R2dbcMybatisConfiguration configuration;
    private final ReactiveMybatisExecutor reactiveMybatisExecutor;

    private DefaultReactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration,
                                             ReactiveMybatisExecutor reactiveMybatisExecutor) {
        this.configuration = configuration;
        this.configuration.initialize();
        this.reactiveMybatisExecutor = reactiveMybatisExecutor;
    }

    /**
     * New default reactive sql session factory builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public ReactiveSqlSession openSession(ReactiveSqlSessionProfile reactiveSqlSessionProfile) {
        return new DefaultReactiveSqlSession(this.configuration, reactiveMybatisExecutor, reactiveSqlSessionProfile);
    }

    @Override
    public R2dbcMybatisConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void close() throws Exception {
        if (this.configuration.getR2dbcEnvironment().getConnectionFactory() instanceof Closeable) {
            Closeable closeableConnectionFactory = ((Closeable) this.configuration.getR2dbcEnvironment().getConnectionFactory());
            closeableConnectionFactory.close();
        }
    }

    /**
     * The type Builder.
     */
    public static class Builder {

        private R2dbcMybatisConfiguration r2dbcMybatisConfiguration;
        private ReactiveMybatisExecutor reactiveMybatisExecutor;

        /**
         * Target R2dbcMybatisConfiguration Must Not Be Null
         *
         * @param r2dbcMybatisConfiguration the r2dbc mybatis configuration
         * @return builder
         */
        public Builder withR2dbcMybatisConfiguration(R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
            Objects.requireNonNull(r2dbcMybatisConfiguration, "R2dbcMybatisConfiguration Could not be null");
            this.r2dbcMybatisConfiguration = r2dbcMybatisConfiguration;
            return this;
        }

        /**
         * Specific ReactiveMybatisExecutor
         *
         * @param reactiveMybatisExecutor the reactive mybatis executor
         * @return builder
         */
        public Builder withReactiveMybatisExecutor(ReactiveMybatisExecutor reactiveMybatisExecutor) {
            Objects.requireNonNull(reactiveMybatisExecutor, "ReactiveMybatisExecutor Could not be null");
            this.reactiveMybatisExecutor = reactiveMybatisExecutor;
            return this;
        }

        /**
         * build DefaultReactiveSqlSessionFactory
         *
         * @return default reactive sql session factory
         */
        public DefaultReactiveSqlSessionFactory build() {
            Objects.requireNonNull(this.r2dbcMybatisConfiguration, "R2dbcMybatisConfiguration Could not be null");
            Objects.requireNonNull(this.r2dbcMybatisConfiguration.getR2dbcEnvironment(), "R2dbcEnvironment of R2dbcMybatisConfiguration Could not be null");
            if (Objects.nonNull(this.reactiveMybatisExecutor)) {
                return new DefaultReactiveSqlSessionFactory(this.r2dbcMybatisConfiguration,
                        this.reactiveMybatisExecutor
                );
            }
            return new DefaultReactiveSqlSessionFactory(this.r2dbcMybatisConfiguration,
                    new DefaultReactiveMybatisExecutor(this.r2dbcMybatisConfiguration)
            );
        }
    }
}

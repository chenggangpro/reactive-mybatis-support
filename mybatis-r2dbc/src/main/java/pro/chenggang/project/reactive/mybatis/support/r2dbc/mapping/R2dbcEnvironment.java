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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping;

import io.r2dbc.spi.ConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.connection.DefaultTransactionSupportConnectionFactory;

/**
 * The type r2dbc environment instead of original {@link org.apache.ibatis.mapping.Environment}
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public class R2dbcEnvironment {

    private final String id;
    private final ConnectionFactory connectionFactory;

    private R2dbcEnvironment(String id, ConnectionFactory connectionFactory) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        this.id = id;
        if (connectionFactory == null) {
            throw new IllegalArgumentException(
                    "Parameter 'dataSource' which refer to ConnectionFactory's type must not be null");
        }
        this.connectionFactory = connectionFactory;
    }


    public static class Builder {

        private final String id;
        private ConnectionFactory connectionFactory;
        private boolean usingDefaultTransactionProxy = true;

        public Builder(String id) {
            this.id = id;
        }

        /**
         * Connection factory.
         *
         * @param connectionFactory the connection factory
         * @return the r2dbc environment builder
         */
        public R2dbcEnvironment.Builder connectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        /**
         * whether using default transaction proxy or not ,default is true
         *
         * @param usingDefault the using default
         * @return builder
         */
        public R2dbcEnvironment.Builder withDefaultTransactionProxy(boolean usingDefault) {
            this.usingDefaultTransactionProxy = usingDefault;
            return this;
        }

        public String id() {
            return this.id;
        }

        public R2dbcEnvironment build() {
            if (usingDefaultTransactionProxy) {
                ConnectionFactory transactionSupportConnectionFactory = new DefaultTransactionSupportConnectionFactory(
                        this.connectionFactory);
                return new R2dbcEnvironment(this.id, transactionSupportConnectionFactory);
            }
            return new R2dbcEnvironment(this.id, this.connectionFactory);
        }

    }

    public String getId() {
        return this.id;
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}

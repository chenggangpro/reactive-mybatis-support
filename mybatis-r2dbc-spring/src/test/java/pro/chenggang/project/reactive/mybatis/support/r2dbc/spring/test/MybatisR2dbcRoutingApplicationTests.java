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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.testcontainers.DatabaseInitialization.R2dbcProtocol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

@TestInstance(PER_CLASS)
public class MybatisR2dbcRoutingApplicationTests extends MybatisR2dbcBaseTests {

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        R2dbcProtocol mysqlR2dbcProtocol = setUp(MySQLContainer.class, false);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].name", MySQLContainer.class::getSimpleName);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].as-default", () -> Boolean.TRUE);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].r2dbc-url", mysqlR2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].username", mysqlR2dbcProtocol.getDatabaseConfig()::getUsername);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].password", mysqlR2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.routing.definitions[0].pool.validation-query", mysqlR2dbcProtocol::getValidationQuery);
        R2dbcProtocol mariadbR2dbcProtocol = setUp(MariaDBContainer.class, false);
        registry.add("spring.r2dbc.mybatis.routing.definitions[1].name", MariaDBContainer.class::getSimpleName);
        registry.add("spring.r2dbc.mybatis.routing.definitions[1].r2dbc-url", mariadbR2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.routing.definitions[1].username", mariadbR2dbcProtocol.getDatabaseConfig()::getUsername);
        registry.add("spring.r2dbc.mybatis.routing.definitions[1].password", mariadbR2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.routing.definitions[1].pool.validation-query", mariadbR2dbcProtocol::getValidationQuery);
        R2dbcProtocol postgresr2dbcProtocol = setUp(PostgreSQLContainer.class, false);
        registry.add("spring.r2dbc.mybatis.routing.definitions[2].name", PostgreSQLContainer.class::getSimpleName);
        registry.add("spring.r2dbc.mybatis.routing.definitions[2].r2dbc-url", postgresr2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.routing.definitions[2].username", postgresr2dbcProtocol.getDatabaseConfig()::getUsername);
        registry.add("spring.r2dbc.mybatis.routing.definitions[2].password", postgresr2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.routing.definitions[2].pool.validation-query", postgresr2dbcProtocol::getValidationQuery);
        R2dbcProtocol mssqlR2dbcProtocol = setUp(MSSQLServerContainer.class, false);
        registry.add("spring.r2dbc.mybatis.routing.definitions[3].name", MSSQLServerContainer.class::getSimpleName);
        registry.add("spring.r2dbc.mybatis.routing.definitions[3].r2dbc-url", mssqlR2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.routing.definitions[3].username", mssqlR2dbcProtocol.getDatabaseConfig()::getUsername);
        registry.add("spring.r2dbc.mybatis.routing.definitions[3].password", mssqlR2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.routing.definitions[3].pool.validation-query", mssqlR2dbcProtocol::getValidationQuery);
        R2dbcProtocol oracleR2dbcProtocol = setUp(OracleContainer.class, false);
        registry.add("spring.r2dbc.mybatis.routing.definitions[4].name", OracleContainer.class::getSimpleName);
        registry.add("spring.r2dbc.mybatis.routing.definitions[4].r2dbc-url", oracleR2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.routing.definitions[4].username", oracleR2dbcProtocol.getDatabaseConfig()::getUsername);
        registry.add("spring.r2dbc.mybatis.routing.definitions[4].password", oracleR2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.routing.definitions[4].pool.validation-query", oracleR2dbcProtocol::getValidationQuery);
    }

    @Autowired
    protected ReactiveTransactionManager transactionManager;

    protected TransactionalOperator transactionalOperator() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        return TransactionalOperator.create(transactionManager, definition);
    }

    protected TransactionalOperator transactionalOperator(Consumer<DefaultTransactionDefinition> transactionDefinitionCustomizer) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        transactionDefinitionCustomizer.accept(definition);
        return TransactionalOperator.create(transactionManager, definition);
    }

    public <T> Mono<T> withRollback(final Mono<T> publisher) {
        return this.transactionalOperator()
                .execute(tx -> {
                    tx.setRollbackOnly();
                    return publisher;
                })
                .singleOrEmpty();
    }

    public <T> Flux<T> withRollback(final Flux<T> publisher) {
        return this.transactionalOperator()
                .execute(tx -> {
                    tx.setRollbackOnly();
                    return publisher;
                });
    }

    @Disabled
    @Test
    void testContext() {

    }
}


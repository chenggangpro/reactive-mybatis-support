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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testcontainers.containers.MariaDBContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

@ActiveProfiles("standard")
@TestInstance(PER_CLASS)
public class MybatisR2dbcApplicationTests extends MybatisR2dbcBaseTests {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String envDatabaseType = System.getProperty("databaseType",
                MariaDBContainer.class.getSimpleName()
        );
        databaseInitializationContainer.keySet()
                .stream()
                .filter(databaseType -> databaseType.getSimpleName().equalsIgnoreCase(envDatabaseType))
                .findFirst()
                .ifPresent(databaseType -> setUp(databaseType, false));
        registry.add("spring.r2dbc.mybatis.r2dbc-url", r2dbcProtocol::getProtocolUrl);
        registry.add("spring.r2dbc.mybatis.password", r2dbcProtocol.getDatabaseConfig()::getPassword);
        registry.add("spring.r2dbc.mybatis.username", r2dbcProtocol.getDatabaseConfig()::getUsername);
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

    @Test
    void testContext() {

    }
}


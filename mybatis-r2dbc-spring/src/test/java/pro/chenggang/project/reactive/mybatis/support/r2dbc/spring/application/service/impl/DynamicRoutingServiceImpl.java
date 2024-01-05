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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.ApplicationService;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.DynamicRoutingService;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context.R2dbcMybatisDatabaseRoutingOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicRoutingServiceImpl implements DynamicRoutingService {

    private final ApplicationService applicationService;
    private final ReactiveTransactionManager reactiveTransactionManager;

    @Override
    public Mono<Void> runWithDynamicRoutingWithoutTransaction() {
        Mono<Void> mysqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MySQLContainer.class.getSimpleName(),
                applicationService.runWithoutTransaction()
        );
        Mono<Void> mariadbExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MariaDBContainer.class.getSimpleName(),
                applicationService.runWithoutTransaction()
        );
        Mono<Void> postgresExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                PostgreSQLContainer.class.getSimpleName(),
                applicationService.runWithoutTransaction()
        );
        Mono<Void> mssqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MSSQLServerContainer.class.getSimpleName(),
                applicationService.runWithoutTransaction()
        );
        return Flux.concat(mysqlExecution, mariadbExecution, postgresExecution, mssqlExecution)
                .then();
    }

    @Override
    public Mono<Void> runWithDynamicRoutingWithTransactionCommit() {
        Mono<Void> mysqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MySQLContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionCommit());
                })
        );
        Mono<Void> mariadbExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MariaDBContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionCommit());
                })
        );
        Mono<Void> postgresExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                PostgreSQLContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionCommit());
                })
        );
        Mono<Void> mssqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MSSQLServerContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionCommit());
                })
        );
        return Flux.concat(mysqlExecution, mariadbExecution, postgresExecution, mssqlExecution)
                .then();
    }

    @Override
    public Mono<Void> runWithDynamicRoutingWithTransactionRollback() {
        Mono<Void> mysqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MySQLContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionRollback());
                })
        );
        Mono<Void> mariadbExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MariaDBContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionRollback());
                })
        );
        Mono<Void> postgresExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                PostgreSQLContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionRollback());
                })
        );
        Mono<Void> mssqlExecution = R2dbcMybatisDatabaseRoutingOperator.executeMono(
                MSSQLServerContainer.class.getSimpleName(),
                Mono.defer(() -> {
                    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                    definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    definition.setName(UUID.randomUUID().toString());
                    return TransactionalOperator.create(this.reactiveTransactionManager, definition)
                            .transactional(applicationService.runWithTransactionRollback());
                })
        );
        return Flux.concatDelayError(mysqlExecution, mariadbExecution, postgresExecution, mssqlExecution)
                .then();
    }
}

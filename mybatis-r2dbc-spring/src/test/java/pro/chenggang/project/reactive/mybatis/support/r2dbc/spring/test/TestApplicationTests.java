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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.TestApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

@TestInstance(PER_CLASS)
@SpringBootTest(classes = TestApplication.class)
public class TestApplicationTests {

	@BeforeAll
	public void contextLoads() {
		Hooks.onOperatorDebug();
	}

	@Autowired
	private ReactiveTransactionManager transactionManager;

	protected TransactionalOperator transactionalOperator(){
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
		definition.setName(UUID.randomUUID().toString());
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

}


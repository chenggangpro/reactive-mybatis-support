package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.TestApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

@SpringBootTest(classes = TestApplication.class)
public class TestApplicationTests {

	@Test
	public void contextLoads() {

	}

	@Autowired
	private ReactiveTransactionManager transactionManager;

	private TransactionalOperator transactionalOperator(){
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
				.next();
	}

	public <T> Flux<T> withRollback(final Flux<T> publisher) {
		return this.transactionalOperator()
				.execute(tx -> {
					tx.setRollbackOnly();
					return publisher;
				});
	}

}


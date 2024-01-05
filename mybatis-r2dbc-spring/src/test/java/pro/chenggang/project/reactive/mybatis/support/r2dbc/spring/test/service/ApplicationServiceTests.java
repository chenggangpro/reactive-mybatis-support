package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.ApplicationService;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class ApplicationServiceTests extends MybatisR2dbcApplicationTests {

    @Autowired
    ApplicationService applicationService;

    @Test
    void runWithoutTransaction() {
        applicationService.runWithoutTransaction()
                .as(StepVerifier::create)
                .verifyComplete();

    }

    @Test
    void runWithTransactionCommit() {
        applicationService.runWithTransactionCommit()
                .as(super::withRollback)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void runWithTransactionRollback() {
        applicationService.runWithTransactionRollback()
                .as(super::withRollback)
                .as(StepVerifier::create)
                .expectError(IllegalStateException.class)
                .verify();
    }
}

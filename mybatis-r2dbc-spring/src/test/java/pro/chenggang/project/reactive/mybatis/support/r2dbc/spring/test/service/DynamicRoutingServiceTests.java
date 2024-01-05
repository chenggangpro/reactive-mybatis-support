package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.DynamicRoutingService;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcRoutingApplicationTests;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

import java.util.List;

/**
 * @author evans
 * @version 1.0.0
 * @since 1.0.0
 */
@ActiveProfiles("routing")
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class DynamicRoutingServiceTests extends MybatisR2dbcRoutingApplicationTests {

    @Autowired
    DynamicRoutingService dynamicRoutingService;

    @Test
    void runWithDynamicRoutingWithoutTransaction() {
        dynamicRoutingService.runWithDynamicRoutingWithoutTransaction()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void runWithDynamicRoutingWithTransactionCommit() {
        dynamicRoutingService.runWithDynamicRoutingWithTransactionCommit()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void runWithDynamicRoutingWithTransactionRollback() {
        dynamicRoutingService.runWithDynamicRoutingWithTransactionRollback()
                .as(StepVerifier::create)
                .verifyErrorSatisfies(throwable -> {
                    Assertions.assertTrue(Exceptions.isMultiple(throwable));
                    List<Throwable> throwableList = Exceptions.unwrapMultipleExcludingTracebacks(throwable);
                    Assertions.assertEquals(4,throwableList.size());
                    for (Throwable th : throwableList) {
                        Assertions.assertInstanceOf(IllegalStateException.class,th);
                    }
                });
    }

}

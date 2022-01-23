package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.entity.extend.DeptWithEmp;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gang Cheng
 */
public class BusinessServiceTests extends TestApplicationTests {

    @Autowired
    private BusinessService businessService;

    @Test
    public void testDoWithoutTransaction() throws Exception {
        int count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                businessService.doWithoutTransaction()
                        .as(StepVerifier::create)
                        .expectNextCount(1)
                        .verifyComplete();
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
    }

    @Test
    public void testDoWithTransactionBusiness() {
        businessService.doWithTransactionBusiness()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testDoWithTransactionBusinessRollback() throws Exception {
        businessService.doWithTransactionBusinessRollback()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> {
                    return "manually rollback with @Transaction".equals(throwable.getMessage());
                })
                .verify();
    }

    @Test
    public void testDoWithoutTransactionThenWithTransaction() {
        businessService.doWithoutTransaction()
                .flatMap(deptWithEmp -> {
                    assertThat(deptWithEmp)
                            .extracting(DeptWithEmp::getEmpList)
                            .matches(empList -> empList.size() > 0);
                    return businessService.doWithTransactionBusinessRollback();
                })
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> {
                    return "manually rollback with @Transaction".equals(throwable.getMessage());
                })
                .verify();
    }
}

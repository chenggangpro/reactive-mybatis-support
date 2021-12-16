package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.BusinessService;
import reactor.test.StepVerifier;

/**
 * @author: chenggang
 * @date 7/6/21.
 */
public class BusinessServiceTests extends TestApplicationTests {

    @Autowired
    private BusinessService businessService;

    @Test
    public void testDoWithTransactionBusiness(){
        businessService.doWithTransactionBusiness()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testDoWithTransactionBusinessRollback() throws Exception{
        businessService.doWithTransactionBusinessRollback()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectError()
                .verify();
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.PeopleMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.service.PeopleBusinessService;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: chenggang
 * @date 7/6/21.
 */
public class PeopleBusinessServiceTests extends TestApplicationTests {

    @Autowired
    private PeopleBusinessService peopleBusinessService;

    @Autowired
    private PeopleMapper peopleMapper;

    @Test
    public void testDoWithTransactionBusiness(){
        peopleBusinessService.doWithTransactionBusiness()
                .as(this::withRollback)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testDoWithTransactionBusinessRollback(){
        peopleBusinessService.doWithTransactionBusinessRollback()
                .as(StepVerifier::create)
                .assertNext(people ->  assertThat(people.getNick()).isEqualTo("mybatis"))
                .verifyComplete();
    }
}

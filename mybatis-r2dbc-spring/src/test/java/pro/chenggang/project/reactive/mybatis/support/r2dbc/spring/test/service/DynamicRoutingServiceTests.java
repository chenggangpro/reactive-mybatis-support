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

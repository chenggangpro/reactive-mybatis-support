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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.query.dynamic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.dynamic.DynamicQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class DynamicQueryMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    DynamicQueryMapper dynamicQueryMapper;

    @Test
    void countAllDept() {
        dynamicQueryMapper.countAllDept()
                .as(StepVerifier::create)
                .assertNext(result -> assertEquals(result, 4))
                .verifyComplete();
    }

    @Test
    void selectByDeptNo() {
        dynamicQueryMapper.selectByDeptNo(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(dept.getDeptNo(), 1L);
                })
                .verifyComplete();
    }

    @Test
    void selectAllDept() {
        dynamicQueryMapper.selectAllDept()
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(dept.getDeptNo(), 1L);
                })
                .assertNext(dept -> {
                    assertEquals(dept.getDeptNo(), 2L);
                })
                .assertNext(dept -> {
                    assertEquals(dept.getDeptNo(), 3L);
                })
                .assertNext(dept -> {
                    assertEquals(dept.getDeptNo(), 4L);
                })
                .verifyComplete();
    }

}

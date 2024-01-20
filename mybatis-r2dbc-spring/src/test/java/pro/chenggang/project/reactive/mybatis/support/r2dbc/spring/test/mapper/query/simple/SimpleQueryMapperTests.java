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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.query.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.simple.SimpleQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class SimpleQueryMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    SimpleQueryMapper simpleQueryMapper;

    @Test
    void countAll() {
        simpleQueryMapper.countAllDept()
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void selectOne() {
        simpleQueryMapper.selectOneDept()
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectByDeptNo() {
        simpleQueryMapper.selectByDeptNo(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectByDeptNoWithAnnotation() {
        simpleQueryMapper.selectByDeptNoWithAnnotatedResult(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectByDeptNoWithResultMap() {
        simpleQueryMapper.selectByDeptNoWithResultMap(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectByDeptNoWithConstructMap() {
        simpleQueryMapper.selectByDeptNoWithConstructorResultMap(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectByDeptNoWithAnnotatedConstruct() {
        simpleQueryMapper.selectByDeptNoWithAnnotatedConstructor(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectDeptWithEmpList() {
        simpleQueryMapper.selectDeptWithEmpList(1L)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                    Assertions.assertNotNull(dept.getEmpList());
                    assertEquals(3, dept.getEmpList()
                            .size());
                })
                .verifyComplete();
    }

    @Test
    void selectEmpWithDept() {
        simpleQueryMapper.selectEmpWithDept(1L)
                .as(StepVerifier::create)
                .assertNext(emp -> {
                    assertEquals(1L, emp.getEmpNo(), 1L);
                    Assertions.assertNotNull(emp.getDept());
                    assertEquals(2L, emp.getDept()
                            .getDeptNo());
                })
                .verifyComplete();
    }

}

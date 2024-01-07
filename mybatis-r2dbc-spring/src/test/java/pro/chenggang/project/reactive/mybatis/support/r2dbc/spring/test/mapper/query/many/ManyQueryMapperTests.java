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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.query.many;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.query.many.ManyQueryMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class ManyQueryMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    ManyQueryMapper manyQueryMapper;

    @Test
    void selectAllDept() {
        manyQueryMapper.selectAllDept("TI")
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                })
                .assertNext(dept -> {
                    assertEquals(4L, dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void selectAllDeptWithEmpList() {
        manyQueryMapper.selectAllDeptWithEmpList()
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertEquals(3, dept.getEmpList()
                            .size());
                })
                .assertNext(dept -> {
                    assertEquals(2L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertEquals(5, dept.getEmpList()
                            .size());
                })
                .assertNext(dept -> {
                    assertEquals(3L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertEquals(6, dept.getEmpList()
                            .size());
                })
                .assertNext(dept -> {
                    assertEquals(4L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertTrue(dept.getEmpList().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void selectAllDeptWithEmpListWithoutOrdered() {
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 15
        manyQueryMapper.selectAllDeptWithEmpList()
                .take(1, true)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertEquals(3, dept.getEmpList()
                            .size());
                })
                .verifyComplete();
    }

    @Test
    void selectAllDeptWithEmpListOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 4
        manyQueryMapper.selectAllDeptWithEmpListOrdered()
                .take(1, true)
                .as(StepVerifier::create)
                .assertNext(dept -> {
                    assertEquals(1L, dept.getDeptNo());
                    assertNotNull(dept.getEmpList());
                    assertEquals(3, dept.getEmpList()
                            .size());
                })
                .verifyComplete();
    }

    @Test
    void selectAllEmpWithOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        manyQueryMapper.selectAllEmpWithOrdered("S")
                .take(1, true)
                .as(StepVerifier::create)
                .assertNext(emp -> {
                    assertEquals(1L, emp.getEmpNo());
                })
                .verifyComplete();
    }

    @Test
    void selectAllEmpWithDept() {
        manyQueryMapper.selectAllEmpWithDept()
                .as(StepVerifier::create)
                .assertNext(emp -> {
                    assertEquals(1L, emp.getEmpNo());
                    assertNotNull(emp.getDept());
                    assertEquals(2L, emp.getDept()
                            .getDeptNo());
                })
                .assertNext(emp -> {
                    assertEquals(2L, emp.getEmpNo());
                    assertNotNull(emp.getDept());
                    assertEquals(3L, emp.getDept()
                            .getDeptNo());
                })
                .expectNextCount(12)
                .verifyComplete();
    }

    @Test
    void selectAllEmpWithDeptWithoutOrdered() {
        // this should consume all rows for nested result map
        // then discard not-required data
        // the mybatis log count should be 14
        manyQueryMapper.selectAllEmpWithDept()
                .take(1, true) // take count;
                .as(StepVerifier::create)
                .assertNext(emp -> {
                    assertEquals(1L, emp.getEmpNo());
                    assertNotNull(emp.getDept());
                })
                .verifyComplete();
    }

    @Test
    void selectAllEmpWithDeptOrdered() {
        // this should only take n+1 rows
        // (N represents the number of rows that need to be read to reach the maximum row count)
        // (An additional row is included to check whether the nested result map should cache the next entry )
        // then discard not-required data
        // the mybatis log count should be 2
        manyQueryMapper.selectAllEmpWithDeptOrdered()
                .take(1, true) // take count;
                .as(StepVerifier::create)
                .assertNext(emp -> {
                    assertEquals(1L, emp.getEmpNo());
                    assertNotNull(emp.getDept());
                })
                .verifyComplete();
    }


}

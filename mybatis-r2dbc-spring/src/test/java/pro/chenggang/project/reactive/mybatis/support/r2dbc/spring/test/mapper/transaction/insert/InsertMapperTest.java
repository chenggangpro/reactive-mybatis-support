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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.transaction.insert;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.transaction.insert.InsertMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.SubjectContent;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
class InsertMapperTest extends MybatisR2dbcApplicationTests {

    Dept dept;

    void resetDept() {
        dept = new Dept();
        dept.setDeptName("INSET_DEPT_NAME");
        dept.setLocation("INSET_DEPT_LOCATION");
        dept.setCreateTime(LocalDateTime.now());
    }

    @Autowired
    InsertMapper insertMapper;

    @Test
    void insertOneDept() {
        this.resetDept();
        insertMapper.insertOneDept(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertOneDeptWithGeneratedKey() {
        this.resetDept();
        insertMapper.insertOneDeptWithGeneratedKey(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNotNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertOneDeptWithGeneratedKeyUsingSelectKey() {
        if (!MySQLContainer.class.equals(currentContainerType) && !MariaDBContainer.class.equals(currentContainerType)) {
            return;
        }
        this.resetDept();
        insertMapper.insertOneDeptWithGeneratedKeyUsingSelectKey(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNotNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertOneDeptWithAnnotation() {
        this.resetDept();
        insertMapper.insertOneDeptWithAnnotation(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertOneDeptWithGeneratedKeyAndAnnotation() {
        this.resetDept();
        insertMapper.insertOneDeptWithGeneratedKeyAndAnnotation(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNotNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertOneDeptWithGeneratedKeyAndAnnotationAndSelectKey() {
        if (!MySQLContainer.class.equals(currentContainerType) && !MariaDBContainer.class.equals(currentContainerType)) {
            return;
        }
        this.resetDept();
        insertMapper.insertOneDeptWithGeneratedKeyAndAnnotationAndSelectKey(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNotNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertMultipleDept() {
        this.resetDept();
        List<Dept> deptList = new ArrayList<>();
        deptList.add(dept);
        deptList.add(dept);
        deptList.add(dept);
        insertMapper.insertMultipleDept(deptList)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(3, effectRowCount);
                })
                .verifyComplete();
    }

    @Test
    void insertMultipleDeptWithAnnotation() {
        this.resetDept();
        List<Dept> deptList = new ArrayList<>();
        deptList.add(dept);
        deptList.add(dept);
        deptList.add(dept);
        insertMapper.insertMultipleDeptWithAnnotation(deptList)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(3, effectRowCount);
                })
                .verifyComplete();
    }

    @Test
    void insertWithDynamic() {
        this.resetDept();
        insertMapper.insertWithDynamic(dept)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertAndGeneratedKeyWithDynamic() {
        this.resetDept();
        insertMapper.insertAndGeneratedKeyWithDynamic(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                    assertNotNull(dept.getDeptNo());
                })
                .verifyComplete();
    }

    @Test
    void insertMultipleWithDynamic() {
        this.resetDept();
        List<Dept> deptList = new ArrayList<>();
        deptList.add(dept);
        deptList.add(dept);
        deptList.add(dept);
        insertMapper.insertMultipleWithDynamic(deptList)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(3, effectRowCount);
                })
                .verifyComplete();
    }

    @Test
    void insertWithBlobAndClod() {
        SubjectContent subjectContent = new SubjectContent();
        subjectContent.setId(3);
        subjectContent.setBlobContent(Blob.from(
                Mono.just(ByteBuffer.wrap("This is a test blob content".getBytes(StandardCharsets.UTF_8)))
        ));
        subjectContent.setClobContent(
                Clob.from(Mono.just("This is a test clob content"))
        );
        insertMapper.insertWithBlobAndClod(subjectContent)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(1, effectRowCount);
                })
                .verifyComplete();
    }

}
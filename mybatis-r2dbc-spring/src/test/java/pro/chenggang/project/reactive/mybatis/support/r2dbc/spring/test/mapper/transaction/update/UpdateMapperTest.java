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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.transaction.update;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.transaction.update.UpdateMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.Dept;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.SubjectContent;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
class UpdateMapperTest extends MybatisR2dbcApplicationTests {

    @Autowired
    UpdateMapper updateMapper;

    @Test
    void updateDeptByDeptNo() {
        Dept dept = new Dept();
        dept.setDeptNo(1L);
        dept.setDeptName("INSET_DEPT_NAME");
        dept.setLocation("INSET_DEPT_LOCATION");
        dept.setCreateTime(LocalDateTime.now());
        updateMapper.updateDeptByDeptNo(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(effectRowCount, 1);
                })
                .verifyComplete();
    }

    @Test
    void updateDeptByDeptNoWithAnnotation() {
        Dept dept = new Dept();
        dept.setDeptNo(1L);
        dept.setDeptName("INSET_DEPT_NAME");
        dept.setLocation("INSET_DEPT_LOCATION");
        dept.setCreateTime(LocalDateTime.now());
        updateMapper.updateDeptByDeptNoWithAnnotation(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(effectRowCount, 1);
                })
                .verifyComplete();
    }

    @Test
    void updateDeptByDeptNoWithDynamic() {
        Dept dept = new Dept();
        dept.setDeptNo(1L);
        dept.setDeptName("INSET_DEPT_NAME");
        dept.setLocation("INSET_DEPT_LOCATION");
        dept.setCreateTime(LocalDateTime.now());
        updateMapper.updateDeptByDeptNoWithDynamic(dept)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(effectRowCount, 1);
                })
                .verifyComplete();
    }

    @Test
    void updateBlobAndClodById() {
        SubjectContent subjectContent = new SubjectContent();
        subjectContent.setId(1);
        subjectContent.setBlobContent(Blob.from(
                Mono.just(ByteBuffer.wrap("This is a test blob content".getBytes(StandardCharsets.UTF_8)))
        ));
        subjectContent.setClobContent(
                Clob.from(Mono.just("This is a test clob content"))
        );
        updateMapper.updateBlobAndClodById(subjectContent)
                .as(super::withRollback)
                .as(StepVerifier::create)
                .consumeNextWith(effectRowCount -> {
                    assertEquals(effectRowCount, 1);
                })
                .verifyComplete();
    }
}
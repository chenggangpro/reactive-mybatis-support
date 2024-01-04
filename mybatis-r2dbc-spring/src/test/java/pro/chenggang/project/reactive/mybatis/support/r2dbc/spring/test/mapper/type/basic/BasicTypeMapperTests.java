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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.type.basic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.basic.BasicTypeMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.entity.SubjectData;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.option.SubjectDataAnEnum;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class BasicTypeMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    BasicTypeMapper basicTypeMapper;

    @Test
    void selectAllSubject() {
        basicTypeMapper.selectAllSubject()
                .as(StepVerifier::create)
                .assertNext(subject -> {
                    assertEquals(1, subject.getId());
                    assertEquals("a", subject.getName());
                    assertEquals(10, subject.getAge());
                    assertEquals(100, subject.getHeight());
                    assertEquals(45, subject.getWeight());
                    assertTrue(subject.getActive());
                    assertEquals(LocalDateTime.of(2023, 1, 1, 1, 1, 1), subject.getDt());
                    assertEquals(22222222222L, subject.getLength());
                })
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void selectAllSubjectData() {
        basicTypeMapper.selectAllSubjectData()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .assertNext(subjectData -> {
                    assertEquals((byte) 2, subjectData.getAbyte());
                    assertEquals((short) 2, subjectData.getAshort());
                    assertEquals("b", subjectData.getAchar());
                    assertEquals(2, subjectData.getAnint());
                    assertEquals(2L, subjectData.getAlong());
                    assertEquals(2, subjectData.getAdouble());
                    assertEquals("b", subjectData.getAstring());
                    assertEquals(SubjectDataAnEnum.B, subjectData.getAnenum());
                    assertEquals(new BigDecimal("10.23"), subjectData.getAdecimal());
                    assertNull(subjectData.getAtimestamp());
                    assertEquals(LocalDate.of(2023, 1, 1), subjectData.getAdate());
                    assertEquals(LocalDateTime.of(2023, 1, 1, 1, 1, 1), subjectData.getAdatetime());
                })
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void selectAllSubjectWithSubjectData() {
        basicTypeMapper.selectAllSubjectWithSubjectData()
                .as(StepVerifier::create)
                .assertNext(subject -> {
                    assertEquals(1, subject.getId());
                    assertEquals("a", subject.getName());
                    assertEquals(10, subject.getAge());
                    assertEquals(100, subject.getHeight());
                    assertEquals(45, subject.getWeight());
                    assertTrue(subject.getActive());
                    assertEquals(LocalDateTime.of(2023, 1, 1, 1, 1, 1), subject.getDt());
                    assertEquals(22222222222L, subject.getLength());
                    assertNotNull(subject.getSubjectDataList());
                    SubjectData subjectData = subject.getSubjectDataList().get(0);
                    // 1, 1, 'a', 1, 1, 1, 1.0, 1, 'a', 'A', 10.23, CURRENT_TIMESTAMP, DATE(NOW()), NOW()
                    assertEquals((byte) 1, subjectData.getAbyte());
                    assertEquals((short) 1, subjectData.getAshort());
                    assertEquals("a", subjectData.getAchar());
                    assertEquals(1, subjectData.getAnint());
                    assertEquals(1L, subjectData.getAlong());
                    assertEquals(1.0, subjectData.getAdouble());
                    assertEquals("a", subjectData.getAstring());
                    assertEquals(SubjectDataAnEnum.A, subjectData.getAnenum());
                    assertEquals(new BigDecimal("10.23"), subjectData.getAdecimal());
                    assertNotNull(subjectData.getAtimestamp());
                    assertEquals(LocalDate.now(), subjectData.getAdate());
                    assertEquals(LocalDate.now(), subjectData.getAdatetime().toLocalDate());
                })
                .expectNextCount(3)
                .verifyComplete();
    }
}

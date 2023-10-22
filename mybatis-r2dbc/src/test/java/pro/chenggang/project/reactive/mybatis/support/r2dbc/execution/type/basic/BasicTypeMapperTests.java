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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.type.basic;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.Subject;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectData;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectDataAnEnum;
import pro.chenggang.project.reactive.mybatis.support.common.entity.extend.SubjectWithSubjectData;

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
public class BasicTypeMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void selectAllSubject() {
        super.<Subject>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(BasicTypeMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    BasicTypeMapper basicTypeMapper = reactiveSqlSession.getMapper(BasicTypeMapper.class);
                    return basicTypeMapper.selectAllSubject();
                })
                .verifyWith(firstStep -> firstStep
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
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllSubjectData() {
        super.<SubjectData>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(BasicTypeMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    BasicTypeMapper basicTypeMapper = reactiveSqlSession.getMapper(BasicTypeMapper.class);
                    return basicTypeMapper.selectAllSubjectData();
                })
                .verifyWith(firstStep -> firstStep
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
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllSubjectWithSubjectData() {
        super.<SubjectWithSubjectData>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(BasicTypeMapper.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                })
                .runWith((type, reactiveSqlSession) -> {
                    BasicTypeMapper basicTypeMapper = reactiveSqlSession.getMapper(BasicTypeMapper.class);
                    return basicTypeMapper.selectAllSubjectWithSubjectData();
                })
                .verifyWith(firstStep -> firstStep
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
                        .verifyComplete()
                )
                .run();
    }
}

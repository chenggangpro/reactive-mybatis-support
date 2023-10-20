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
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectDataAEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                            assertEquals(subject.getId(), 1);
                            assertEquals(subject.getName(), "a");
                            assertEquals(subject.getAge(), 10);
                            assertEquals(subject.getHeight(), 100);
                            assertEquals(subject.getWeight(), 45);
                            assertTrue(subject.getActive());
                            assertEquals(subject.getDt(), LocalDateTime.of(2023, 1, 1, 1, 1, 1));
                            assertEquals(subject.getLength(), 22222222222L);
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
                            assertEquals(subjectData.getAbyte(), (byte) 2);
                            assertEquals(subjectData.getAshort(), (short) 2);
                            assertEquals(subjectData.getAchar(), "b");
                            assertEquals(subjectData.getAnint(), 2);
                            assertEquals(subjectData.getAlong(), 2L);
                            assertEquals(subjectData.getAdouble(), 2);
                            assertEquals(subjectData.getAstring(), "b");
                            assertEquals(subjectData.getAnenum(), SubjectDataAEnum.B);
                            assertEquals(subjectData.getAdecimal(), new BigDecimal("10.23"));
                            assertNull(subjectData.getAtimestamp());
                            assertEquals(subjectData.getAdate(), LocalDate.of(2023, 1, 1));
                            assertEquals(subjectData.getAdatetime(), LocalDateTime.of(2023, 1, 1, 1, 1, 1));
                        })
                        .expectNextCount(1)
                        .verifyComplete()
                )
                .run();
    }
}

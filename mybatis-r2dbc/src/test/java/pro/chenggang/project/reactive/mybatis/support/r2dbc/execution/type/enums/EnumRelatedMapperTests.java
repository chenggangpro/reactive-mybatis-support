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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.type.enums;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectData;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectDataAnEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class EnumRelatedMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void selectByAnEnum() {
        super.<SubjectData>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(EnumTypeHandler.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(EnumRelatedMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    EnumRelatedMapper enumRelatedMapper = reactiveSqlSession.getMapper(EnumRelatedMapper.class);
                    return enumRelatedMapper.selectByAnEnum(SubjectDataAnEnum.B);
                })
                .verifyWith(firstStep -> firstStep
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
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByAnEnumOrdinal() {
        super.<SubjectData>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(EnumOrdinalTypeHandler.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(EnumRelatedMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    EnumRelatedMapper enumRelatedMapper = reactiveSqlSession.getMapper(EnumRelatedMapper.class);
                    return enumRelatedMapper.selectByAnEnumOrdinal(SubjectDataAnEnum.B);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(subjectData -> {
                            // Specific resultMap named 'anIntToEnumResultMap' in EnumRelatedMapper.xml
                            assertEquals((byte) 1, subjectData.getAbyte());
                            assertEquals((short) 1, subjectData.getAshort());
                            assertEquals("a", subjectData.getAchar());
                            assertNull(subjectData.getAnint());
                            assertEquals(1L, subjectData.getAlong());
                            assertEquals(1, subjectData.getAdouble());
                            assertEquals("a", subjectData.getAstring());
                            assertEquals(SubjectDataAnEnum.B, subjectData.getAnenum());
                            assertEquals(new BigDecimal("10.23"), subjectData.getAdecimal());
                            assertNotNull(subjectData.getAtimestamp());
                            assertEquals(LocalDate.now(), subjectData.getAdate());
                            assertEquals(LocalDate.now(),
                                    subjectData.getAdatetime()
                                            .toLocalDate()
                            );
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllAnEnum() {
        super.<SubjectDataAnEnum>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(EnumTypeHandler.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(EnumRelatedMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    EnumRelatedMapper enumRelatedMapper = reactiveSqlSession.getMapper(EnumRelatedMapper.class);
                    return enumRelatedMapper.selectAllAnEnum();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(subjectDataAnEnum -> {
                            assertEquals(SubjectDataAnEnum.A, subjectDataAnEnum);
                        })
                        .assertNext(subjectDataAnEnum -> {
                            assertEquals(SubjectDataAnEnum.B, subjectDataAnEnum);
                        })
                        .assertNext(subjectDataAnEnum -> {
                            assertEquals(SubjectDataAnEnum.C, subjectDataAnEnum);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllAnIntAsAnEnumOrdinal() {
        super.<SubjectDataAnEnum>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(EnumOrdinalTypeHandler.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(EnumRelatedMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    EnumRelatedMapper enumRelatedMapper = reactiveSqlSession.getMapper(EnumRelatedMapper.class);
                    return enumRelatedMapper.selectAllAnIntAsAnEnumOrdinal();
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(subjectDataAnEnum -> {
                            assertEquals(SubjectDataAnEnum.B, subjectDataAnEnum);
                        })
                        .assertNext(subjectDataAnEnum -> {
                            assertEquals(SubjectDataAnEnum.C, subjectDataAnEnum);
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectByAnEnumOrdinalSpecificEnumType() {
        super.<SpecificEnumType>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(EnumOrdinalTypeHandler.class);
                    r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry().register(SpecificEnumTypeR2dbcTypeHandlerAdapter.class);
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(EnumRelatedMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    EnumRelatedMapper enumRelatedMapper = reactiveSqlSession.getMapper(EnumRelatedMapper.class);
                    return enumRelatedMapper.selectByAnEnumOrdinalSpecificEnumType(SpecificEnumType.A);
                })
                .verifyWith(firstStep -> firstStep
                        .assertNext(specificEnumType -> {
                            assertEquals(SpecificEnumType.A, specificEnumType);
                        })
                        .verifyComplete()
                )
                .run();
    }
}

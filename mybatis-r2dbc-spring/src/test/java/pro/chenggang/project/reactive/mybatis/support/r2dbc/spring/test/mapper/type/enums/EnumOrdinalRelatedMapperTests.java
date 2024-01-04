
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.type.enums;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.enums.EnumRelatedMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.enums.SpecificEnumType;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.enums.SpecificEnumTypeR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.common.option.SubjectDataAnEnum;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class EnumOrdinalRelatedMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    EnumRelatedMapper enumRelatedMapper;

    @Autowired
    ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("r2dbc.mybatis.configuration.defaultEnumTypeHandler", EnumOrdinalTypeHandler.class::getName);
    }

    @Test
    void selectByAnEnumOrdinal() {
        enumRelatedMapper.selectByAnEnumOrdinal(SubjectDataAnEnum.B)
                .as(StepVerifier::create)
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
                .verifyComplete();
    }


    @Test
    void selectAllAnIntAsAnEnumOrdinal() {
        enumRelatedMapper.selectAllAnIntAsAnEnumOrdinal()
                .as(StepVerifier::create)
                .assertNext(subjectDataAnEnum -> {
                    assertEquals(SubjectDataAnEnum.B, subjectDataAnEnum);
                })
                .assertNext(subjectDataAnEnum -> {
                    assertEquals(SubjectDataAnEnum.C, subjectDataAnEnum);
                })
                .verifyComplete();
    }

    @Test
    void selectByAnEnumOrdinalSpecificEnumType() {
        reactiveSqlSessionFactory.getConfiguration().getR2dbcTypeHandlerAdapterRegistry()
                .register(SpecificEnumTypeR2dbcTypeHandlerAdapter.class);
        enumRelatedMapper.selectByAnEnumOrdinalSpecificEnumType(SpecificEnumType.A)
                .as(StepVerifier::create)
                .assertNext(specificEnumType -> {
                    assertEquals(SpecificEnumType.A, specificEnumType);
                })
                .verifyComplete();
    }

}

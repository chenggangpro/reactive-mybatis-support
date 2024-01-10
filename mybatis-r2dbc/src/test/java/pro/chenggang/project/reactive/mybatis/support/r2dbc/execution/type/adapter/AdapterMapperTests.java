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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.type.adapter;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import pro.chenggang.project.reactive.mybatis.support.MybatisR2dbcBaseTests;
import pro.chenggang.project.reactive.mybatis.support.common.entity.SubjectContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class AdapterMapperTests extends MybatisR2dbcBaseTests {

    @Test
    void selectAllAStringAsBlob() {
        super.<Blob>newTestRunner()
                .allDatabases()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    AdapterMapper adapterMapper = reactiveSqlSession.getMapper(AdapterMapper.class);
                    return adapterMapper.selectAllAStringAsBlob()
                            .collectList()
                            .flatMapMany(Flux::fromIterable);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(blob -> {
                            Mono.from(blob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext(ByteBuffer.wrap("This is a blob content1".getBytes(StandardCharsets.UTF_8)))
                                    .verifyComplete();
                        })
                        .consumeNextWith(blob -> {
                            Mono.from(blob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext(ByteBuffer.wrap("This is a blob content2".getBytes(StandardCharsets.UTF_8)))
                                    .verifyComplete();
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAStringAsBlobByAString() {
        // oracle doesn't support blob on where condition
        // link https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/Comparison-Conditions.html#GUID-828576BF-E606-4EA6-B94B-BFF48B67F927
        super.<Blob>newTestRunner()
                .filterDatabases(databaseType -> !OracleContainer.class.equals(databaseType))
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    AdapterMapper adapterMapper = reactiveSqlSession.getMapper(AdapterMapper.class);
                    return adapterMapper.selectAStringAsBlobByAString(Blob.from(Mono.just(ByteBuffer.wrap(
                            "This is a blob content1".getBytes(StandardCharsets.UTF_8)))));
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(blob -> {
                            Mono.from(blob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext(ByteBuffer.wrap("This is a blob content1".getBytes(StandardCharsets.UTF_8)))
                                    .verifyComplete();
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAllAStringAsClob() {
        super.<Clob>newTestRunner()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    AdapterMapper adapterMapper = reactiveSqlSession.getMapper(AdapterMapper.class);
                    return adapterMapper.selectAllAStringAsClob()
                            .collectList()
                            .flatMapMany(Flux::fromIterable);
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(clob -> {
                            Mono.from(clob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext("This is a clob content1")
                                    .verifyComplete();
                        })
                        .consumeNextWith(clob -> {
                            Mono.from(clob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext("This is a clob content2")
                                    .verifyComplete();
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAStringAsClobByAString() {
        // oracle doesn't support clob on where condition
        // link https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/Comparison-Conditions.html#GUID-828576BF-E606-4EA6-B94B-BFF48B67F927
        super.<Clob>newTestRunner()
                .filterDatabases(databaseType -> !OracleContainer.class.equals(databaseType))
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    AdapterMapper adapterMapper = reactiveSqlSession.getMapper(AdapterMapper.class);
                    return adapterMapper.selectAStringAsClobByAString(Clob.from(Mono.just(
                            "This is a clob content1")));
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(clob -> {
                            Mono.from(clob.stream())
                                    .as(StepVerifier::create)
                                    .expectNext("This is a clob content1")
                                    .verifyComplete();
                        })
                        .verifyComplete()
                )
                .run();
    }

    @Test
    void selectAll() {
        super.<SubjectContent>newTestRunner()
                .customizeR2dbcConfiguration(r2dbcMybatisConfiguration -> {
                    r2dbcMybatisConfiguration.setMapUnderscoreToCamelCase(true);
                    r2dbcMybatisConfiguration.addMapper(AdapterMapper.class);
                })
                .runWith((type, reactiveSqlSession) -> {
                    AdapterMapper adapterMapper = reactiveSqlSession.getMapper(AdapterMapper.class);
                    return adapterMapper.selectAll();
                })
                .verifyWith(firstStep -> firstStep
                        .consumeNextWith(subjectContent -> {
                            Assertions.assertEquals(1, subjectContent.getId());
                            Mono.from(subjectContent.getBlobContent()
                                            .stream())
                                    .as(StepVerifier::create)
                                    .expectNext(ByteBuffer.wrap("This is a blob content1".getBytes(StandardCharsets.UTF_8)))
                                    .verifyComplete();
                            Mono.from(subjectContent.getClobContent()
                                            .stream())
                                    .as(StepVerifier::create)
                                    .expectNext("This is a clob content1")
                                    .verifyComplete();
                        })
                        .consumeNextWith(subjectContent -> {
                            Assertions.assertEquals(2, subjectContent.getId());
                            Mono.from(subjectContent.getBlobContent()
                                            .stream())
                                    .as(StepVerifier::create)
                                    .expectNext(ByteBuffer.wrap("This is a blob content2".getBytes(StandardCharsets.UTF_8)))
                                    .verifyComplete();
                            Mono.from(subjectContent.getClobContent()
                                            .stream())
                                    .as(StepVerifier::create)
                                    .expectNext("This is a clob content2")
                                    .verifyComplete();
                        })
                        .verifyComplete()
                )
                .run();
    }
}

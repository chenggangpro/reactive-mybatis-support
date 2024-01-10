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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.mapper.type.adapter;

import io.r2dbc.spi.Blob;
import io.r2dbc.spi.Clob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.OracleContainer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.MybatisR2dbcApplication;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.type.adapter.AdapterMapper;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test.MybatisR2dbcApplicationTests;
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
@SpringBootTest(classes = MybatisR2dbcApplication.class)
public class AdapterMapperTests extends MybatisR2dbcApplicationTests {

    @Autowired
    AdapterMapper adapterMapper;

    @Test
    void selectAllAStringAsBlob() {
        adapterMapper.selectAllAStringAsBlob()
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .as(StepVerifier::create)
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
                .verifyComplete();
    }

    @Test
    void selectAStringAsBlobByAString() {
        if(OracleContainer.class.equals(currentContainerType)){
            // oracle doesn't support blob on where condition
            // link https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/Comparison-Conditions.html#GUID-828576BF-E606-4EA6-B94B-BFF48B67F927
            return;
        }
        adapterMapper.selectAStringAsBlobByAString(Blob.from(Mono.just(ByteBuffer.wrap(
                        "This is a blob content1".getBytes(StandardCharsets.UTF_8)))))
                .as(StepVerifier::create)
                .consumeNextWith(blob -> {
                    Mono.from(blob.stream())
                            .as(StepVerifier::create)
                            .expectNext(ByteBuffer.wrap("This is a blob content1".getBytes(StandardCharsets.UTF_8)))
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @Test
    void selectAllAStringAsClob() {
        adapterMapper.selectAllAStringAsClob()
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .as(StepVerifier::create)
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
                .verifyComplete();
    }

    @Test
    void selectAStringAsClobByAString() {
        if(OracleContainer.class.equals(currentContainerType)){
            // oracle doesn't support clob on where condition
            // link https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/Comparison-Conditions.html#GUID-828576BF-E606-4EA6-B94B-BFF48B67F927
            return;
        }
        adapterMapper.selectAStringAsClobByAString(Clob.from(Mono.just(
                        "This is a clob content1")))
                .as(StepVerifier::create)
                .consumeNextWith(clob -> {
                    Mono.from(clob.stream())
                            .as(StepVerifier::create)
                            .expectNext("This is a clob content1")
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @Test
    void selectAll() {
        adapterMapper.selectAll()
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .as(StepVerifier::create)
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
                .verifyComplete();
    }
}

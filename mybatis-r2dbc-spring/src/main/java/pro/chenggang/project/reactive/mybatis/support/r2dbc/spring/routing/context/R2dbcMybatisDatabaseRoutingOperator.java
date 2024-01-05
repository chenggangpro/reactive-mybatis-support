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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The r2dbc mybatis database routing operator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class R2dbcMybatisDatabaseRoutingOperator {

    /**
     * Execute mono with target routing key.
     *
     * @param <T>              the type parameter
     * @param targetRoutingKey the target routing key
     * @param mono             the mono execution
     * @return the mono execution surround with auto routing operation
     */
    public static <T> Mono<T> executeMono(String targetRoutingKey, Mono<T> mono) {
        return R2dbcMybatisDatabaseRoutingContextManager.currentRoutingHolderContext()
                .flatMap(databaseRoutingContextHolder -> Mono.usingWhen(Mono.just(databaseRoutingContextHolder),
                        routingContextHolder -> {
                            try {
                                return mono;
                            } catch (Throwable ex) {
                                return Mono.error(ex);
                            }
                        },
                        routingContextHolder -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        },
                        (routingContextHolder, err) -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        },
                        routingContextHolder -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        }
                ).onErrorResume(ex -> {
                    R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = databaseRoutingContextHolder.getDatabaseRoutingKeys()
                            .pollFirst();
                    log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                    return Mono.error(ex);
                }))
                .contextWrite(R2dbcMybatisDatabaseRoutingContextManager.initializeDatabaseRoutingContext(targetRoutingKey))
                .contextWrite(R2dbcMybatisDatabaseRoutingContextManager.initializeDatabaseRoutingHolderContext());
    }

    /**
     * Execute flux with target routing key.
     *
     * @param <T>              the type parameter
     * @param targetRoutingKey the target routing key
     * @param flux             the flux execution
     * @return the flux execution surround with auto routing operation
     */
    public static <T> Flux<T> executeFlux(String targetRoutingKey, Flux<T> flux) {
        return R2dbcMybatisDatabaseRoutingContextManager.currentRoutingHolderContext()
                .flatMapMany(databaseRoutingContextHolder -> Flux.usingWhen(Mono.just(databaseRoutingContextHolder),
                        routingContextHolder -> {
                            try {
                                return flux;
                            } catch (Throwable ex) {
                                return Mono.error(ex);
                            }
                        },
                        routingContextHolder -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        },
                        (routingContextHolder, err) -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        },
                        routingContextHolder -> {
                            R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = routingContextHolder.getDatabaseRoutingKeys()
                                    .pollFirst();
                            log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                            return Mono.empty();
                        }
                ).onErrorResume(ex -> {
                    R2dbcMybatisDatabaseRoutingKeyInfo oldKeyInfo = databaseRoutingContextHolder.getDatabaseRoutingKeys()
                            .pollFirst();
                    log.debug("Reset Current DataSourceType Key : {}", oldKeyInfo);
                    return Flux.error(ex);
                }))
                .contextWrite(R2dbcMybatisDatabaseRoutingContextManager.initializeDatabaseRoutingContext(targetRoutingKey))
                .contextWrite(R2dbcMybatisDatabaseRoutingContextManager.initializeDatabaseRoutingHolderContext());
    }

}

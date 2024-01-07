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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import org.apache.ibatis.mapping.MappedStatement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;
import reactor.core.publisher.Mono;

/**
 * The type No key r 2 dbc key generator.
 *
 * @author Gang Cheng
 * @version 1.0.2
 * @since 1.0.2
 */
public class NoKeyR2dbcKeyGenerator implements R2dbcKeyGenerator {

    private NoKeyR2dbcKeyGenerator() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NoKeyR2dbcKeyGenerator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public KeyGeneratorType keyGeneratorType() {
        return KeyGeneratorType.NONE;
    }

    @Override
    public Mono<Boolean> processSelectKey(KeyGeneratorType keyGeneratorType, MappedStatement ms, Object parameter) {
        return Mono.just(true);
    }

    @Override
    public Long processGeneratedKeyResult(RowResultWrapper rowResultWrapper, Object parameter) {
        return 0L;
    }

    private static class InstanceHolder {

        private final static NoKeyR2dbcKeyGenerator INSTANCE;

        static {
            INSTANCE = new NoKeyR2dbcKeyGenerator();
        }

    }
}

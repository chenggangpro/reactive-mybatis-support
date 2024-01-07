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
package pro.chenggang.project.reactive.mybatis.support.generator.core.context;

import org.mybatis.generator.config.Context;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;

/**
 * The Context generator.
 *
 * @author Gang Cheng
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ContextGenerator {

    /**
     * generator type
     *
     * @return generator type
     */
    GeneratorType targetGeneratorType();

    /**
     * Generate generator context
     *
     * @param generatorProperties the generator properties
     * @return context
     */
    Context generateContext(GeneratorProperties generatorProperties);
}

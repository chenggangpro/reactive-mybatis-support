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

import pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl.MyBatisSimpleContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl.MyBatisSimpleModelContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl.MyBatisSimpleModelXmlContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl.MybatisDynamicContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl.MybatisDynamicMapperContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The Context Generator configuration factory.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ContextGeneratorFactory {

    private final Map<GeneratorType, ContextGenerator> contextGeneratorContainer = new HashMap<>();

    public ContextGeneratorFactory() {
        this.registerContextGenerator(new MyBatisSimpleContextGenerator());
        this.registerContextGenerator(new MybatisDynamicContextGenerator());
        this.registerContextGenerator(new MyBatisSimpleModelContextGenerator());
        this.registerContextGenerator(new MyBatisSimpleModelXmlContextGenerator());
        this.registerContextGenerator(new MybatisDynamicMapperContextGenerator());
    }

    /**
     * Register context generator.
     *
     * @param contextGenerator the context generator
     */
    public void registerContextGenerator(ContextGenerator contextGenerator) {
        this.contextGeneratorContainer.put(contextGenerator.targetGeneratorType(), contextGenerator);
    }

    /**
     * get context generator
     *
     * @param generatorType the generator type
     * @return the optional ContextGenerator
     */
    public Optional<ContextGenerator> getContextGenerator(GeneratorType generatorType) {
        return Optional.ofNullable(this.contextGeneratorContainer.get(generatorType));
    }

}

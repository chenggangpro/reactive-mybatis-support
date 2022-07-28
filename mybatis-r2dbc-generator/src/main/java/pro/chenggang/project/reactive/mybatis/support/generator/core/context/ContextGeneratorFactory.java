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

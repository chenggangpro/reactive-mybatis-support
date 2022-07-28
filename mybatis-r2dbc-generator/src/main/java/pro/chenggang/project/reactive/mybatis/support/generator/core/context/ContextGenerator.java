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

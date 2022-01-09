package pro.chenggang.project.reactive.mybatis.support.generator.core.context;

import org.mybatis.generator.config.Context;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorExtensionProperties;

/**
 * The interface Context generator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface ContextGenerator {

    /**
     * generator type
     *
     * @return generator type
     */
    GeneratorType targetGeneratorType();

    /**
     * generateContext
     *
     * @param extensionProperties the extension properties
     * @return context
     */
    Context generateContext(GeneratorExtensionProperties extensionProperties);
}

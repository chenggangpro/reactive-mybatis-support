package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator.Configurer;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class FluentGeneratorPropertiesLoader implements GeneratorPropertiesLoader {

    private final GeneratorPropertiesBuilder generatorPropertiesBuilder;

    public FluentGeneratorPropertiesLoader(Configurer configurer) {
        this.generatorPropertiesBuilder = new GeneratorPropertiesBuilder(configurer);
    }

    public FluentGeneratorPropertiesLoader(GeneratorPropertiesBuilder generatorPropertiesBuilder) {
        this.generatorPropertiesBuilder = generatorPropertiesBuilder;
    }

    public GeneratorPropertiesBuilder getGeneratorPropertiesBuilder() {
        return this.generatorPropertiesBuilder;
    }

    @Override
    public GeneratorProperties load() {
        return this.generatorPropertiesBuilder.build();
    }

}

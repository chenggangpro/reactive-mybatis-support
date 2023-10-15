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
package pro.chenggang.project.reactive.mybatis.support.generator.core;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.internal.DefaultShellCallback;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.ContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.ContextGeneratorFactory;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.GeneratedJavaTypeModifier;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.FluentGeneratorPropertiesLoader;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorPropertiesBuilder;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorPropertiesHolder;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorPropertiesLoader;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.YamlGeneratorPropertiesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Mybatis dynamic code generator builder.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class MybatisDynamicCodeGenerator {

    private final ContextGeneratorFactory contextGeneratorFactory = new ContextGeneratorFactory();

    private MybatisDynamicCodeGenerator() {

    }

    private MybatisDynamicCodeGenerator(GeneratorPropertiesLoader generatorPropertiesLoader) {
        GeneratorPropertiesHolder.getInstance()
                .setGeneratorPropertiesLoader(generatorPropertiesLoader);
    }

    /**
     * With specific generator properties loader
     *
     * @param generatorPropertiesLoader the generator properties loader
     * @return the mybatis dynamic code generator
     */
    public static MybatisDynamicCodeGenerator withGeneratorPropertiesLoader(GeneratorPropertiesLoader generatorPropertiesLoader) {
        return new MybatisDynamicCodeGenerator(generatorPropertiesLoader);
    }

    /**
     * With default yaml configuration file in classpath
     * <br>
     * <li>mybatis-generator.yaml</li>
     * <li>mybatis-generator.yml</li>
     *
     * @return the mybatis dynamic code generator
     */
    public static MybatisDynamicCodeGenerator withYamlConfiguration() {
        return new MybatisDynamicCodeGenerator(new YamlGeneratorPropertiesLoader());
    }

    /**
     * With generator properties builder
     *
     * @return the generator properties builder
     */
    public static GeneratorPropertiesBuilder withGeneratorPropertiesBuilder() {
        FluentGeneratorPropertiesLoader fluentGeneratorPropertiesLoader = new FluentGeneratorPropertiesLoader(new Configurer(new MybatisDynamicCodeGenerator()));
        GeneratorPropertiesHolder.getInstance().setGeneratorPropertiesLoader(fluentGeneratorPropertiesLoader);
        return fluentGeneratorPropertiesLoader.getGeneratorPropertiesBuilder();
    }

    /**
     * With target yaml configuration file name.
     * <br>
     * <li>1. classpath:config.yaml will load by classloader</li>
     * <li>2. /xxx/config.yaml will load by file system</li>
     *
     * @param configurationFileName the configuration file name
     * @return the mybatis dynamic code generator
     */
    public static MybatisDynamicCodeGenerator withYamlConfiguration(String configurationFileName) {
        if (StringUtils.isBlank(configurationFileName)) {
            throw new IllegalArgumentException("The configuration file name can not be blank");
        }
        if (StringUtils.endsWithAny(configurationFileName, "yaml", "yml")) {
            throw new IllegalArgumentException("The configuration file type must be yaml or yml");
        }
        return new MybatisDynamicCodeGenerator(new YamlGeneratorPropertiesLoader(configurationFileName));
    }

    /**
     * Execute generate action.
     */
    public void generate() {
        GeneratorProperties generatorProperties = GeneratorPropertiesHolder.getInstance().getGeneratorProperties();
        generatorProperties.validate();
        Configuration configuration = this.getConfiguration(generatorProperties);
        DefaultShellCallback callback = new DefaultShellCallback(generatorProperties.isOverwrite());
        List<String> warnings = new ArrayList<>();
        try {
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, callback, warnings);
            myBatisGenerator.generate(new VerboseProgressCallback());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        warnings.forEach(System.out::println);
    }

    /**
     * get configuration
     *
     * @return the configuration
     */
    private Configuration getConfiguration(GeneratorProperties generatorProperties) {
        Configuration configuration = new Configuration();
        generatorProperties
                .getGeneratorTypes()
                .stream()
                .map(this.contextGeneratorFactory::getContextGenerator)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(contextGenerator -> contextGenerator.generateContext(generatorProperties))
                .forEach(configuration::addContext);
        return configuration;
    }

    /**
     * Custom configure.
     *
     * @return the configurer
     */
    public Configurer customConfigure() {
        return new Configurer(this);
    }

    /**
     * The Configurer.
     */
    public static class Configurer {

        private final MybatisDynamicCodeGenerator mybatisDynamicCodeGenerator;

        public Configurer(MybatisDynamicCodeGenerator mybatisDynamicCodeGenerator) {
            this.mybatisDynamicCodeGenerator = mybatisDynamicCodeGenerator;
        }

        /**
         * Configure properties.
         *
         * @return the configurer
         */
        public GeneratorPropertiesBuilder customizeGeneratorProperties() {
            return new GeneratorPropertiesBuilder(GeneratorPropertiesHolder.getInstance().getGeneratorProperties(), this);
        }

        /**
         * Configure generate base package
         *
         * @param baseLocation the base location
         * @param basePackage  the base package
         * @return the configurer
         */
        public Configurer configureGenerateBasePackage(String baseLocation, String basePackage) {
            return new GeneratorPropertiesBuilder(GeneratorPropertiesHolder.getInstance().getGeneratorProperties(), this)
                    .targetLocationBuilder()
                    .baseLocation(baseLocation)
                    .thenBuilder()
                    .targetPackageBuilder()
                    .basePackage(basePackage)
                    .thenPropertiesBuilder()
                    .thenConfigurer();
        }

        /**
         * Configure generated java type modifier
         *
         * @param generatedJavaTypeModifierClass the type extend GeneratedJavaTypeModifier class
         * @return the configurer
         */
        public Configurer configureGeneratedJavaTypeModifier(Class<? extends GeneratedJavaTypeModifier> generatedJavaTypeModifierClass) {
            return new GeneratorPropertiesBuilder(GeneratorPropertiesHolder.getInstance().getGeneratorProperties(), this)
                    .generatedJavaTypeModifierClass(generatedJavaTypeModifierClass)
                    .thenConfigurer();
        }

        /**
         * Configure base package from specific class
         *
         * @param specificClass the specific class
         * @return the configurer
         */
        public Configurer applyGenerateBasePackageFromClass(Class<?> specificClass) {
            return new GeneratorPropertiesBuilder(GeneratorPropertiesHolder.getInstance().getGeneratorProperties(), this)
                    .targetPackageBuilder()
                    .basePackage(specificClass.getPackage().getName())
                    .thenPropertiesBuilder()
                    .thenConfigurer();
        }


        /**
         * Configure context generator.
         *
         * @param contextGenerator the context generator
         * @return the configurer
         */
        public Configurer registerContextGenerator(ContextGenerator contextGenerator) {
            this.mybatisDynamicCodeGenerator.contextGeneratorFactory.registerContextGenerator(contextGenerator);
            return this;
        }

        /**
         * Return mybatis dynamic code generator.
         *
         * @return the mybatis dynamic code generator
         */
        public MybatisDynamicCodeGenerator toGenerator() {
            return this.mybatisDynamicCodeGenerator;
        }
    }
}

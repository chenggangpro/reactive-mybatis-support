package pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;

/**
 * The Mybatis simple context generator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class MyBatisSimpleModelContextGenerator extends AbstractCommonContextGenerator {

    @Override
    public GeneratorType targetGeneratorType() {
        return GeneratorType.MODEL;
    }

    @Override
    protected Context newContext() {
        Context context = new Context(ModelType.FLAT);
        context.setTargetRuntime("MyBatis3Simple");
        context.setId("MyBatis3Simple");
        context.addProperty("javaFileEncoding", "UTF-8");
        context.addProperty("columnOverride", "false");
        return context;
    }

    @Override
    protected void configureSqlMapGenerator(Context context, GeneratorProperties generatorProperties) {
        // disable original operation
    }

    @Override
    protected void configureJavaClientGenerator(Context context, GeneratorProperties generatorProperties) {
        // disable original operation
    }
}

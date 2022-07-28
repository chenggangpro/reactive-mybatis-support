package pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;

/**
 * The Mybatis simple context generator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class MyBatisSimpleContextGenerator extends AbstractCommonContextGenerator {

    @Override
    public GeneratorType targetGeneratorType() {
        return GeneratorType.SIMPLE;
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

}

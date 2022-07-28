package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator;

/**
 * @author Gang Cheng
 */
public class MyBatisGeneratorTests {

    @Test
    public void testGenerate() {
        MybatisDynamicCodeGenerator.withYamlConfiguration()
                .customConfigure()
                .applyGenerateBasePackageFromClass(MyBatisGeneratorTests.class)
                .customizeGeneratorProperties()
                .targetPackageBuilder()
                .basePackage("pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application")
                .thenPropertiesBuilder()
                .thenConfigurer()
                .toGenerator()
                .generate();
    }

}

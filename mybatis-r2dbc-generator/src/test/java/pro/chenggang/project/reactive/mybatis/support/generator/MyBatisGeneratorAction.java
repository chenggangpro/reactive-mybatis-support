package pro.chenggang.project.reactive.mybatis.support.generator;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator;

import java.io.File;

/**
 * Mybatis generator action
 *
 * @author Gang Cheng
 */
public class MyBatisGeneratorAction {

    /**
     * generate through main method
     *
     * @param args args
     */
    public static void main(String[] args) {
        String codeAbsoluteLocation = new File("").getAbsolutePath() + "/mybatis-r2dbc-generator";
        MybatisDynamicCodeGenerator.withYamlConfiguration()
                .customConfigure()
                .configureGenerateBasePackage(codeAbsoluteLocation, "pro.chenggang.project.reactive.mybatis.support.generator")
                .configureGeneratedJavaTypeModifier(TestJavaTypeModifier.class)
                .toGenerator()
                .generate();
    }

    /**
     * generate through test case
     */
    @Test
    public void generateWithYamlWithJunitTestMethod() {
        MybatisDynamicCodeGenerator.withYamlConfiguration()
                .customConfigure()
                .applyGenerateBasePackageFromClass(MyBatisGeneratorAction.class)
                .configureGeneratedJavaTypeModifier(TestJavaTypeModifier.class)
                .toGenerator()
                .generate();
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator;

/**
 * @author Gang Cheng
 */
public class MyBatisGeneratorTests {

    @Test
    public void testGenerate() {
        MybatisDynamicCodeGenerator.getInstance().generate("pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application");
    }

}

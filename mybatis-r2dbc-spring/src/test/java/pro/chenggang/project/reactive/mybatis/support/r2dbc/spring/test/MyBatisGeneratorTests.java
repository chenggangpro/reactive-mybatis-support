package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.test;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.core.MybatisDynamicCodeGenerator;

/**
 * @author: chenggang
 * @date 2020-03-22.
 */
public class MyBatisGeneratorTests {

    @Test
    public void testGenerate(){
        MybatisDynamicCodeGenerator.getInstance().generate("pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application");
    }

}

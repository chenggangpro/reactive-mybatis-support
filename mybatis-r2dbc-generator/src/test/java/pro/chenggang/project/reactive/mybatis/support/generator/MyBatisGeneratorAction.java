package pro.chenggang.project.reactive.mybatis.support.generator;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator;

/**
 * @author Gang Cheng
 */
public class MyBatisGeneratorAction {

    @Test
    public void generate() {
        MybatisDynamicCodeGenerator.getInstance().generate(MyBatisGeneratorAction.class);
    }

}

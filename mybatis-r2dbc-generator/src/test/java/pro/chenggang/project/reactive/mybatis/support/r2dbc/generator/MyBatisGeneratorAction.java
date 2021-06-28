package pro.chenggang.project.reactive.mybatis.support.r2dbc.generator;

import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.core.MybatisDynamicCodeGenerator;

/**
 * @author: chenggang
 * @date 2020-03-22.
 */
public class MyBatisGeneratorAction {

    @Test
    public void generate(){
        MybatisDynamicCodeGenerator.getInstance().generate(MyBatisGeneratorAction.class);
    }

}

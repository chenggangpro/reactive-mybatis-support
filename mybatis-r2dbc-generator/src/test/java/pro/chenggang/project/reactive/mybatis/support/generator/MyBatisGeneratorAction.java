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

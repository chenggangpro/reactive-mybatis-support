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

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.GeneratedJavaTypeModifier;

import java.sql.Types;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class TestJavaTypeModifier implements GeneratedJavaTypeModifier {

    @Override
    public FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        if (Types.TINYINT == column.getJdbcType()) {
            return new FullyQualifiedJavaType(Integer.class.getName());
        }
        return defaultType;
    }
}

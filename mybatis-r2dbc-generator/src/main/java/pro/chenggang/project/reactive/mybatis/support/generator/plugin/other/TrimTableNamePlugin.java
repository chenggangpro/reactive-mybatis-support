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
package pro.chenggang.project.reactive.mybatis.support.generator.plugin.other;

import lombok.NoArgsConstructor;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * The type Trim table name plugin.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
@NoArgsConstructor
public class TrimTableNamePlugin extends PluginAdapter {

    private String prefix;

    @Override
    public boolean validate(List<String> warnings) {
        String trimPrefix = properties.getProperty("tablePrefix");
        boolean valid = stringHasValue(trimPrefix);
        if (valid) {
            prefix = upperFirstLetter(trimPrefix);
        } else {
            if (!stringHasValue(trimPrefix)) {
                warnings.add(getString("ValidationError.18",
                        "TrimTableNamePlugin",
                        "trimPrefix"));
            }
        }
        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        introspectedTable.setMyBatis3JavaMapperType(trimPrefix(introspectedTable.getMyBatis3JavaMapperType()));
        introspectedTable.setMyBatisDynamicSqlSupportType(trimPrefix(introspectedTable.getMyBatisDynamicSqlSupportType()));
        introspectedTable.setMyBatis3XmlMapperFileName(trimPrefix(introspectedTable.getMyBatis3XmlMapperFileName()));


    }

    private String trimPrefix(String name) {
        String[] split = name.split("\\.");
        if (split.length == 0) {
            return name;
        }
        String last = split[split.length - 1];
        if (!StringUtility.stringHasValue(last)) {
            return name;
        }
        if (!last.startsWith(this.prefix)) {
            return name;
        }
        last = last.replaceFirst(this.prefix, "");
        split[split.length - 1] = last;
        return String.join(".", split);
    }

    private String upperFirstLetter(String letter) {
        char[] chars = letter.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }
}
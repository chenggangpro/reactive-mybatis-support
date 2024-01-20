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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * The type Rename java mapper plugin.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
@NoArgsConstructor
public class RenameJavaMapperPlugin extends PluginAdapter {
    private String replaceString;
    private Pattern pattern;

    @Override
    public boolean validate(List<String> warnings) {
        String searchString = properties.getProperty("searchString");
        this.replaceString = properties.getProperty("replaceString");
        boolean valid = stringHasValue(searchString) && stringHasValue(replaceString);
        if (valid) {
            pattern = Pattern.compile(searchString);
        } else {
            if (!stringHasValue(searchString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameJavaMapperPlugin",
                        "searchString"));
            }
            if (!stringHasValue(replaceString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameJavaMapperPlugin",
                        "replaceString"));
            }
        }
        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getMyBatis3JavaMapperType();
        Matcher matcher = pattern.matcher(oldType);
        oldType = matcher.replaceAll(replaceString);
        introspectedTable.setMyBatis3JavaMapperType(oldType);
    }
}
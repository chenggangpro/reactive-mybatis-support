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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Microsoft SQL Server placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class SQLServerPlaceholderDialect implements NamePlaceholderDialect {

    /**
     * The dialect name
     */
    public static final String DIALECT_NAME = "Microsoft SQL Server";

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\.|[^@$\\d\\w_]");

    @Override
    public String name() {
        return DIALECT_NAME;
    }

    @Override
    public String getMarker() {
        // see io.r2dbc.mssql.ParametrizedMssqlStatement$PARAMETER_MATCHER
        // the parameter pattern is "@([\\p{Alpha}@][@$\\d\\w_]{0,127})"
        // in order to adapt original mybatis's parameters like "__frch_item_0.xxx" which pared from <foreach> label
        return "@Ms_";
    }

    @Override
    public String propertyNamePostProcess(String propertyName) {
        if (Objects.isNull(propertyName) || propertyName.length() == 0) {
            return propertyName;
        }
        return PROPERTY_PATTERN.matcher(propertyName).replaceAll("_");
    }
}

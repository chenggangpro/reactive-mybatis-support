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
package pro.chenggang.project.reactive.mybatis.support.generator.plugin.type;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The custom java type resolver.
 */
public class CustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    private final ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    private final Map<String, GeneratedJavaTypeModifier> generatedJavaTypeModifierContainer = new ConcurrentHashMap<>();
    private String generatedJavaTypeModifierType;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.generatedJavaTypeModifierType = properties.getProperty("generatedJavaTypeModifierType","");
    }

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType defaultOverrideType = super.overrideDefaultType(column, defaultType);
        return this.getGeneratedJavaTypeModifier()
                .map(modifier -> modifier.overrideDefaultType(column, defaultOverrideType))
                .orElse(defaultOverrideType);

    }

    /**
     * get generated java type modifier
     *
     * @return the optional GeneratedJavaTypeModifier
     */
    private Optional<GeneratedJavaTypeModifier> getGeneratedJavaTypeModifier() {
        return Optional.ofNullable(this.generatedJavaTypeModifierType)
                .filter(StringUtils::isNotBlank)
                .map(modifierType -> generatedJavaTypeModifierContainer
                        .computeIfAbsent(modifierType, type -> {
                            Class<?> aClass = null;
                            try {
                                aClass = Class.forName(type);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException("Can not find GeneratedJavaTypeModifier Class ," +
                                        "ClassName: " + type, e);
                            }
                            Reflector reflector = reflectorFactory.findForClass(aClass);
                            try {
                                return (GeneratedJavaTypeModifier) reflector.getDefaultConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Can not initialize GeneratedJavaTypeModifier " +
                                        "with default constructor ,ClassName:" + type, e);
                            }
                        })
                );
    }

}

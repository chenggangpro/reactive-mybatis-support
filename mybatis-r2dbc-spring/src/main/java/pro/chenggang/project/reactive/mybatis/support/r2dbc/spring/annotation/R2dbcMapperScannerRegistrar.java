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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.ClassPathR2dbcMapperScanner;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperFactoryBean;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperScannerConfigurer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link ImportBeanDefinitionRegistrar} to allow annotation configuration of MyBatis mapper scanning. Using
 * an @Enable annotation allows beans to be registered via @Component configuration, whereas implementing
 * {@code BeanDefinitionRegistryPostProcessor} will work for XML configuration.
 * <p>
 * copy from original MapperScannerRegistrar
 *
 * @author Michael Lanyon
 * @author Eduardo Macarron
 * @author Putthiphong Boonphong
 * @see R2dbcMapperFactoryBean
 * @see ClassPathR2dbcMapperScanner
 * @since 1.2.0
 */
public class R2dbcMapperScannerRegistrar implements ImportBeanDefinitionRegistrar {

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
        return importingClassMetadata.getClassName() + "#" + R2dbcMapperScannerRegistrar.class.getSimpleName() + "#" + index;
    }

    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(R2dbcMapperScan.class.getName()));
        if (mapperScanAttrs != null) {
            registerBeanDefinitions(importingClassMetadata, mapperScanAttrs, registry, generateBaseBeanName(importingClassMetadata, 0));
        }
    }

    void registerBeanDefinitions(AnnotationMetadata annoMeta, AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(R2dbcMapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", annoAttrs.getBoolean("processPropertyPlaceHolders"));

        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            builder.addPropertyValue("annotationClass", annotationClass);
        }

        Class<?> markerInterface = annoAttrs.getClass("markerInterface");
        if (!Class.class.equals(markerInterface)) {
            builder.addPropertyValue("markerInterface", markerInterface);
        }

        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
        }

        Class<? extends R2dbcMapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!R2dbcMapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
            builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
        }

        String sqlSessionFactoryRef = annoAttrs.getString("sqlSessionFactoryRef");
        if (StringUtils.hasText(sqlSessionFactoryRef)) {
            builder.addPropertyValue("sqlSessionFactoryBeanName", annoAttrs.getString("sqlSessionFactoryRef"));
        }

        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).collect(Collectors.toList()));

        basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName).collect(Collectors.toList()));

        if (basePackages.isEmpty()) {
            basePackages.add(getDefaultBasePackage(annoMeta));
        }

        String lazyInitialization = annoAttrs.getString("lazyInitialization");
        if (StringUtils.hasText(lazyInitialization)) {
            builder.addPropertyValue("lazyInitialization", lazyInitialization);
        }

        String defaultScope = annoAttrs.getString("defaultScope");
        if (!AbstractBeanDefinition.SCOPE_DEFAULT.equals(defaultScope)) {
            builder.addPropertyValue("defaultScope", defaultScope);
        }

        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

    }

    /**
     * A {@link R2dbcMapperScannerRegistrar} for {@link R2dbcMapperScans}.
     *
     * @since 2.0.0
     */
    static class R2dbcRepeatingRegistrar extends R2dbcMapperScannerRegistrar {
        /**
         * {@inheritDoc}
         */
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes mapperScansAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(R2dbcMapperScans.class.getName()));
            if (mapperScansAttrs != null) {
                AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
                for (int i = 0; i < annotations.length; i++) {
                    registerBeanDefinitions(importingClassMetadata, annotations[i], registry, generateBaseBeanName(importingClassMetadata, i));
                }
            }
        }
    }

}

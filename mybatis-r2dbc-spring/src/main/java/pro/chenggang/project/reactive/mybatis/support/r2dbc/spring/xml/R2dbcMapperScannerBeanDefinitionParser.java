/**
 * Copyright 2010-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.xml;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.ClassPathR2dbcMapperScanner;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperFactoryBean;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperScannerConfigurer;

import java.lang.annotation.Annotation;

/**
 * A {#code BeanDefinitionParser} that handles the element scan of the MyBatis. namespace
 *
 * @author Lishu Luo
 * @author Eduardo Macarron
 *
 * @since 1.2.0
 * @see R2dbcMapperFactoryBean
 * @see ClassPathR2dbcMapperScanner
 * @see R2dbcMapperScannerConfigurer
 */

public class R2dbcMapperScannerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_BASE_PACKAGE = "base-package";
    private static final String ATTRIBUTE_ANNOTATION = "annotation";
    private static final String ATTRIBUTE_MARKER_INTERFACE = "marker-interface";
    private static final String ATTRIBUTE_NAME_GENERATOR = "name-generator";
    private static final String ATTRIBUTE_FACTORY_REF = "factory-ref";
    private static final String ATTRIBUTE_MAPPER_FACTORY_BEAN_CLASS = "mapper-factory-bean-class";
    private static final String ATTRIBUTE_LAZY_INITIALIZATION = "lazy-initialization";
    private static final String ATTRIBUTE_DEFAULT_SCOPE = "default-scope";

    /**
     * {@inheritDoc}
     *
     * @since 2.0.2
     */
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(R2dbcMapperScannerConfigurer.class);

        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

        builder.addPropertyValue("processPropertyPlaceHolders", true);
        try {
            String annotationClassName = element.getAttribute(ATTRIBUTE_ANNOTATION);
            if (StringUtils.hasText(annotationClassName)) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) classLoader
                        .loadClass(annotationClassName);
                builder.addPropertyValue("annotationClass", annotationClass);
            }
            String markerInterfaceClassName = element.getAttribute(ATTRIBUTE_MARKER_INTERFACE);
            if (StringUtils.hasText(markerInterfaceClassName)) {
                Class<?> markerInterface = classLoader.loadClass(markerInterfaceClassName);
                builder.addPropertyValue("markerInterface", markerInterface);
            }
            String nameGeneratorClassName = element.getAttribute(ATTRIBUTE_NAME_GENERATOR);
            if (StringUtils.hasText(nameGeneratorClassName)) {
                Class<?> nameGeneratorClass = classLoader.loadClass(nameGeneratorClassName);
                BeanNameGenerator nameGenerator = BeanUtils.instantiateClass(nameGeneratorClass, BeanNameGenerator.class);
                builder.addPropertyValue("nameGenerator", nameGenerator);
            }
            String mapperFactoryBeanClassName = element.getAttribute(ATTRIBUTE_MAPPER_FACTORY_BEAN_CLASS);
            if (StringUtils.hasText(mapperFactoryBeanClassName)) {
                @SuppressWarnings("unchecked")
                Class<? extends R2dbcMapperFactoryBean> mapperFactoryBeanClass = (Class<? extends R2dbcMapperFactoryBean>) classLoader
                        .loadClass(mapperFactoryBeanClassName);
                builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
            }
        } catch (Exception ex) {
            XmlReaderContext readerContext = parserContext.getReaderContext();
            readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
        }

        builder.addPropertyValue("sqlSessionFactoryBeanName", element.getAttribute(ATTRIBUTE_FACTORY_REF));
        builder.addPropertyValue("lazyInitialization", element.getAttribute(ATTRIBUTE_LAZY_INITIALIZATION));
        builder.addPropertyValue("defaultScope", element.getAttribute(ATTRIBUTE_DEFAULT_SCOPE));
        builder.addPropertyValue("basePackage", element.getAttribute(ATTRIBUTE_BASE_PACKAGE));

        return builder.getBeanDefinition();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0.2
     */
    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

}

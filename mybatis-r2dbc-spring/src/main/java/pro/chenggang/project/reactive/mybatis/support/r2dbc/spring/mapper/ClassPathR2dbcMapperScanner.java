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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper;

import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that registers Mappers by {@code basePackage}, {@code annotationClass}, or
 * {@code markerInterface}. If an {@code annotationClass} and/or {@code markerInterface} is specified, only the
 * specified types will be searched (searching for all interfaces will be disabled).
 * <p>
 * This functionality was previously a private class of {@link MapperScannerConfigurer}, but was broken out in version
 * 1.2.0.
 * <p>
 * copy form original ClassPathR2dbcMapperScanner
 *
 * @author Hunter Presnall
 * @author Eduardo Macarron
 * @see R2dbcMapperFactoryBean
 * @since 1.2.0
 */
public class ClassPathR2dbcMapperScanner extends ClassPathBeanDefinitionScanner {

    // Copy of FactoryBean#OBJECT_TYPE_ATTRIBUTE which was added in Spring 5.2
    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathMapperScanner.class);
    private boolean lazyInitialization;

    private ReactiveSqlSessionFactory sqlSessionFactory;

    private String sqlSessionFactoryBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private Class<? extends R2dbcMapperFactoryBean> mapperFactoryBeanClass = R2dbcMapperFactoryBean.class;

    private String defaultScope;

    public ClassPathR2dbcMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Set whether enable lazy initialization for mapper bean.
     * <p>
     * Default is {@code false}.
     * </p>
     *
     * @param lazyInitialization Set the @{code true} to enable
     * @since 2.0.2
     */
    public void setLazyInitialization(boolean lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setSqlSessionFactory(ReactiveSqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    /**
     * Set the {@code MapperFactoryBean} class.
     *
     * @param mapperFactoryBeanClass the {@code MapperFactoryBean} class
     * @since 2.0.1
     */
    public void setMapperFactoryBeanClass(Class<? extends R2dbcMapperFactoryBean> mapperFactoryBeanClass) {
        this.mapperFactoryBeanClass = mapperFactoryBeanClass == null ? R2dbcMapperFactoryBean.class : mapperFactoryBeanClass;
    }

    /**
     * Set the default scope of scanned mappers.
     * <p>
     * Default is {@code null} (equiv to singleton).
     * </p>
     *
     * @param defaultScope the scope
     * @since 2.0.6
     */
    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }

    /**
     * Configures parent scanner to search for the right interfaces. It can search for all interfaces or just for those
     * that extends a markerInterface or/and those annotated with the annotationClass
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // if specified, use the given annotation and / or marker interface
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        // override AssignableTypeFilter to ignore matches on the actual marker interface
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // default include filter that accepts all classes
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        // exclude package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    /**
     * Calls the parent search that will search and register all the candidates. Then the registered objects are post
     * processed to set them as MapperFactoryBeans
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            LOGGER.warn(() -> "No MyBatis mapper was found in '" + Arrays.toString(basePackages)
                    + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        AbstractBeanDefinition definition;
        BeanDefinitionRegistry registry = getRegistry();
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            boolean scopedProxy = false;
            if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
                definition = (AbstractBeanDefinition) Optional
                        .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
                        .map(BeanDefinitionHolder::getBeanDefinition).orElseThrow(() -> new IllegalStateException(
                                "The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
                scopedProxy = true;
            }
            String beanClassName = definition.getBeanClassName();
            LOGGER.debug(() -> "Creating MapperFactoryBean with name '" + holder.getBeanName() + "' and '" + beanClassName
                    + "' mapperInterface");

            // the mapper interface is the original class of the bean
            // but, the actual class of the bean is MapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName); // issue #59
            definition.setBeanClass(this.mapperFactoryBeanClass);

            // Attribute for MockitoPostProcessor
            // https://github.com/mybatis/spring-boot-starter/issues/475
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);

            boolean explicitFactoryUsed = false;
            if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
                definition.getPropertyValues().add("sqlSessionFactory",
                        new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionFactory != null) {
                definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
                explicitFactoryUsed = true;
            }

            if (!explicitFactoryUsed) {
                LOGGER.debug(() -> "Enabling autowire by type for R2dbcMapperFactoryBean with name '" + holder.getBeanName() + "'.");
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            }

            definition.setLazyInit(lazyInitialization);

            if (scopedProxy) {
                continue;
            }

            if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && defaultScope != null) {
                definition.setScope(defaultScope);
            }

            if (!definition.isSingleton()) {
                BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
                if (registry.containsBeanDefinition(proxyHolder.getBeanName())) {
                    registry.removeBeanDefinition(proxyHolder.getBeanName());
                }
                registry.registerBeanDefinition(proxyHolder.getBeanName(), proxyHolder.getBeanDefinition());
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            LOGGER.warn(() -> "Skipping R2dbcMapperFactoryBean with name '" + beanName + "' and '"
                    + beanDefinition.getBeanClassName() + "' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }

}

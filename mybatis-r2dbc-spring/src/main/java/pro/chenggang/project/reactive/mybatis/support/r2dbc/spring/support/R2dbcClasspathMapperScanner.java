package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * R2dbcClasspathMapperScanner
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcClasspathMapperScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger log = LoggerFactory.getLogger(R2dbcClasspathMapperScanner.class);

    private ReactiveSqlSessionFactory sqlSessionFactory;

    private String sqlSessionFactoryBeanName;

    private Class<? extends Annotation> annotationClass;

    private R2dbcMapperFactoryBean<?> mapperFactoryBean = new R2dbcMapperFactoryBean<>();

    /**
     * Instantiates a new r2dbc classpath mapper scanner.
     *
     * @param registry the registry
     */
    public R2dbcClasspathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * Sets annotation class.
     *
     * @param annotationClass the annotation class
     */
    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Sets sql session factory.
     *
     * @param sqlSessionFactory the sql session factory
     */
    public void setSqlSessionFactory(ReactiveSqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Sets sql session factory bean name.
     *
     * @param sqlSessionFactoryBeanName the sql session factory bean name
     */
    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    /**
     * Sets mapper factory bean.
     *
     * @param mapperFactoryBean the mapper factory bean
     */
    public void setMapperFactoryBean(R2dbcMapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean;
    }

    /**
     * Register filters.
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;
        // if specified, use the given annotation and / or marker interface
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
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

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            log.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            if (log.isDebugEnabled()) {
                log.debug("Creating MapperFactoryBean with name '" + holder.getBeanName()
                        + "' and '" + definition.getBeanClassName() + "' mapperInterface");
            }

            // the mapper interface is the original class of the bean
            // but, the actual class of the bean is MapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
            definition.setBeanClass(this.mapperFactoryBean.getClass());

            boolean explicitFactoryUsed = false;
            if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
                definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionFactory != null) {
                definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
                explicitFactoryUsed = true;
            }

            if (!explicitFactoryUsed) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
                }
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
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
            log.warn("Skipping MapperFactoryBean with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' mapperInterface"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }
}

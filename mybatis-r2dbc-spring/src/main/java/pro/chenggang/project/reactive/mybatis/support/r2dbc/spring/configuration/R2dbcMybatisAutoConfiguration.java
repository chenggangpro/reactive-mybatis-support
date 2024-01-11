/*
 *    Copyright 2009-2024 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.configuration;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ValidationDepth;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcXMLMapperBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.converter.MybatisTypeHandlerConverter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcDatabaseIdProvider;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcEnvironment;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation.R2dbcMapperScan;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor.SpringReactiveMybatisExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperFactoryBean;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper.R2dbcMapperScannerConfigurer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.ConnectionFactoryOptionsCustomizer;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.R2dbcMybatisConfigurationCustomizer;
import reactor.core.publisher.Flux;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.context.ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * R2dbc Mybatis Auto Configuration
 *
 * @author Gang Cheng
 * @version 1.0.3
 * @since 1.0.0
 */
@Slf4j
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@AutoConfigureAfter({MybatisLanguageDriverAutoConfiguration.class})
@ConditionalOnClass({ConnectionFactory.class, ReactiveSqlSessionFactory.class, Flux.class})
public class R2dbcMybatisAutoConfiguration {

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

    /**
     * R2dbc connection factory properties r2dbc mybatis connection factory properties.
     *
     * @return the r2dbc mybatis connection factory properties
     */
    @ConfigurationProperties(R2dbcMybatisConnectionFactoryProperties.PREFIX)
    @Bean
    public R2dbcMybatisConnectionFactoryProperties r2dbcConnectionFactoryProperties() {
        return new R2dbcMybatisConnectionFactoryProperties();
    }

    /**
     * Connection factory connection pool.
     *
     * @param r2dbcMybatisConnectionFactoryProperties    the r2dbc mybatis connection factory properties
     * @param connectionFactoryOptionsCustomizerProvider the connection factory options customizer object provider
     * @return the connection pool
     */
    @ConditionalOnProperty(value = "spring.r2dbc.mybatis.routing.enabled", havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean(ConnectionFactory.class)
    @Bean(destroyMethod = "dispose")
    public ConnectionPool connectionFactory(R2dbcMybatisConnectionFactoryProperties r2dbcMybatisConnectionFactoryProperties,
                                            ObjectProvider<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizerProvider) {
        String determineConnectionFactoryUrl = r2dbcMybatisConnectionFactoryProperties.determineConnectionFactoryUrl();
        Assert.notNull(determineConnectionFactoryUrl, "R2DBC Connection URL must not be null");
        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.parse(determineConnectionFactoryUrl);
        //ConnectionFactoryOptionsCustomizer
        List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers = connectionFactoryOptionsCustomizerProvider
                .orderedStream()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(connectionFactoryOptionsCustomizers)) {
            ConnectionFactoryOptions.Builder builder = connectionFactoryOptions.mutate();
            connectionFactoryOptionsCustomizers.forEach(connectionFactoryOptionsCustomizer -> connectionFactoryOptionsCustomizer.customize(
                    builder));
            connectionFactoryOptions = builder.build();
        }
        ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
        if (connectionFactory instanceof ConnectionPool) {
            return (ConnectionPool) connectionFactory;
        }
        R2dbcMybatisConnectionFactoryProperties.Pool pool = r2dbcMybatisConnectionFactoryProperties.getPool();
        ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
                .name(r2dbcMybatisConnectionFactoryProperties.determineConnectionFactoryName())
                .maxSize(pool.getMaxSize())
                .initialSize(pool.getInitialSize())
                .maxIdleTime(pool.getMaxIdleTime())
                .acquireRetry(pool.getAcquireRetry())
                .backgroundEvictionInterval(pool.getBackgroundEvictionInterval())
                .maxAcquireTime(pool.getMaxAcquireTime())
                .maxCreateConnectionTime(pool.getMaxCreateConnectionTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .metricsRecorder(pool.getMetricsRecorder())
                .validationDepth(pool.getValidationDepth());
        if (hasText(pool.getValidationQuery())) {
            builder.validationQuery(pool.getValidationQuery());
        } else {
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        ConnectionPool connectionPool = new ConnectionPool(builder.build());
        log.info("Initialize Connection Pool Success");
        return connectionPool;
    }

    @ConfigurationProperties(R2dbcMybatisProperties.PREFIX)
    @Bean
    public R2dbcMybatisProperties r2dbcMybatisProperties() {
        return new R2dbcMybatisProperties();
    }

    @Bean
    public R2dbcMybatisConfiguration configuration(ConnectionFactory connectionFactory,
                                                   R2dbcMybatisProperties r2dbcMybatisProperties,
                                                   ObjectProvider<TypeHandler<?>> typeHandlerProvider,
                                                   ObjectProvider<R2dbcMybatisConfigurationCustomizer> configurationCustomizerProvider,
                                                   ObjectProvider<R2dbcTypeHandlerAdapter<?>> r2dbcTypeHandlerAdapterProvider,
                                                   ObjectProvider<MybatisTypeHandlerConverter> mybatisTypeHandlerConverterObjectProvider,
                                                   ObjectProvider<LanguageDriver> languageDriversProvider,
                                                   ObjectProvider<R2dbcDatabaseIdProvider> databaseIdProviderObjectProvider) throws Exception {
        R2dbcMybatisConfiguration r2dbcMybatisConfiguration = Optional.ofNullable(r2dbcMybatisProperties.getConfiguration())
                .orElse(new R2dbcMybatisConfiguration());
        R2dbcEnvironment.Builder environmentBuilder = new R2dbcEnvironment.Builder(ReactiveSqlSessionFactory.class.getSimpleName())
                .withDefaultTransactionProxy(false);
        if (!TransactionAwareConnectionFactoryProxy.class.isAssignableFrom(connectionFactory.getClass())) {
            environmentBuilder.connectionFactory(new TransactionAwareConnectionFactoryProxy(connectionFactory));
        } else {
            environmentBuilder.connectionFactory(connectionFactory);
        }
        r2dbcMybatisConfiguration.setR2dbcEnvironment(environmentBuilder.build());
        r2dbcMybatisConfiguration.setVfsImpl(SpringBootVFS.class);
        if (r2dbcMybatisProperties.getConfigurationProperties() != null) {
            r2dbcMybatisConfiguration.setVariables(r2dbcMybatisProperties.getConfigurationProperties());
        }
        // type aliases
        if (StringUtils.hasLength(r2dbcMybatisProperties.getTypeAliasesPackage())) {
            scanClasses(r2dbcMybatisProperties.getTypeAliasesPackage(),
                    r2dbcMybatisProperties.getTypeAliasesSuperType()
            )
                    .stream()
                    .filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> !clazz.isMemberClass())
                    .forEach(r2dbcMybatisConfiguration.getTypeAliasRegistry()::registerAlias);
        }
        if (!ObjectUtils.isEmpty(r2dbcMybatisProperties.getTypeAliases())) {
            Stream.of(r2dbcMybatisProperties.getTypeAliases()).forEach(typeAlias -> {
                r2dbcMybatisConfiguration.getTypeAliasRegistry().registerAlias(typeAlias);
                log.debug("Registered type alias: '" + typeAlias + "'");
            });
        }
        //type handlers
        if (StringUtils.hasLength(r2dbcMybatisProperties.getTypeHandlersPackage())) {
            scanClasses(r2dbcMybatisProperties.getTypeHandlersPackage(), TypeHandler.class)
                    .stream()
                    .filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .forEach(r2dbcMybatisConfiguration.getTypeHandlerRegistry()::register);
        }
        typeHandlerProvider.stream()
                .forEach(typeHandler -> {
                    r2dbcMybatisConfiguration.getTypeHandlerRegistry().register(typeHandler);
                    log.debug("Registered type handler: '" + typeHandler + "'");
                });
        // r2dbc type handler adapter
        if (StringUtils.hasLength(r2dbcMybatisProperties.getR2dbcTypeHandlerAdapterPackage())) {
            scanClasses(r2dbcMybatisProperties.getR2dbcTypeHandlerAdapterPackage(), R2dbcTypeHandlerAdapter.class)
                    .stream()
                    .filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .forEach(clazz -> r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry()
                            .register((Class<? extends R2dbcTypeHandlerAdapter<?>>) clazz)
                    );
        }
        r2dbcTypeHandlerAdapterProvider.stream()
                .forEach(r2dbcTypeHandlerAdapter -> {
                    r2dbcMybatisConfiguration.addR2dbcTypeHandlerAdapter(r2dbcTypeHandlerAdapter);
                    log.debug("Registered r2dbc type handler adapter: '" + r2dbcTypeHandlerAdapter + "'");
                });
        //default enum type handler
        r2dbcMybatisConfiguration.setDefaultEnumTypeHandler(r2dbcMybatisProperties.getDefaultEnumTypeHandler());
        mybatisTypeHandlerConverterObjectProvider.stream()
                .forEach(mybatisTypeHandlerConverter -> {
                    r2dbcMybatisConfiguration.addMybatisTypeHandlerConverter(mybatisTypeHandlerConverter);
                    log.debug("Registered mybatis type handler converter: '" + mybatisTypeHandlerConverter + "'");
                });
        //script language driver
        LanguageDriver[] availableLanguageDrivers = languageDriversProvider.stream().toArray(LanguageDriver[]::new);
        Class<? extends LanguageDriver> defaultLanguageDriver = r2dbcMybatisProperties.getDefaultScriptingLanguageDriver();
        if (!ObjectUtils.isEmpty(availableLanguageDrivers)) {
            Stream.of(availableLanguageDrivers)
                    .forEach(languageDriver -> {
                        r2dbcMybatisConfiguration.getLanguageRegistry().register(languageDriver);
                        log.debug("Registered scripting language driver: '" + languageDriver + "'");
                    });
            if (defaultLanguageDriver == null && availableLanguageDrivers.length == 1) {
                defaultLanguageDriver = availableLanguageDrivers[0].getClass();
            }
        }
        if (defaultLanguageDriver != null) {
            r2dbcMybatisConfiguration.setDefaultScriptingLanguage(defaultLanguageDriver);
        }
        // use R2dbcDatabaseIdProvider instead of the original databaseIdProvider
        R2dbcDatabaseIdProvider r2dbcDatabaseIdProvider = databaseIdProviderObjectProvider.getIfAvailable();
        if (r2dbcMybatisConfiguration.getR2dbcEnvironment() != null && r2dbcDatabaseIdProvider != null) {// fix #64 set databaseId before parse mapper xmls
            r2dbcMybatisConfiguration.setDatabaseId(r2dbcDatabaseIdProvider.getDatabaseId(r2dbcMybatisConfiguration.getR2dbcEnvironment()
                    .getConnectionFactory())
            );
        }
        //mapper scan
        Resource[] mapperLocations = r2dbcMybatisProperties.resolveMapperLocations();
        if (mapperLocations != null) {
            if (mapperLocations.length == 0) {
                log.warn("Property 'mapperLocations' was specified but matching resources are not found.");
            } else {
                for (Resource mapperLocation : mapperLocations) {
                    if (mapperLocation == null) {
                        continue;
                    }
                    try {
                        R2dbcXMLMapperBuilder xmlMapperBuilder = new R2dbcXMLMapperBuilder(mapperLocation.getInputStream(),
                                r2dbcMybatisConfiguration,
                                mapperLocation.toString(),
                                r2dbcMybatisConfiguration.getSqlFragments()
                        );
                        xmlMapperBuilder.parse();
                    } catch (Exception e) {
                        throw new IOException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                    } finally {
                        ErrorContext.instance().reset();
                    }
                    log.debug("Parsed mapper file: '" + mapperLocation + "'");
                }
            }
        } else {
            log.debug("Property 'mapperLocations' was not specified.");
        }
        // R2dbcMybatisConfigurationCustomizer
        configurationCustomizerProvider
                .orderedStream()
                .forEach(r2dbcMybatisConfigurationCustomizer -> r2dbcMybatisConfigurationCustomizer.customize(
                        r2dbcMybatisConfiguration));
        return r2dbcMybatisConfiguration;
    }

    /**
     * scan classes
     *
     * @param packagePatterns the package Patterns
     * @param assignableType  The assignable type
     * @return Class Set
     * @throws IOException Resource IOException
     */
    private Set<Class<?>> scanClasses(String packagePatterns, Class<?> assignableType) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String[] packagePatternArray = tokenizeToStringArray(packagePatterns, CONFIG_LOCATION_DELIMITERS);
        for (String packagePattern : packagePatternArray) {
            Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class");
            for (Resource resource : resources) {
                try {
                    ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource)
                            .getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable e) {
                    log.warn("Cannot load the '" + resource + "'. Cause by " + e.toString());
                }
            }
        }
        return classes;
    }


    @Bean
    @ConditionalOnMissingBean(ReactiveTransactionManager.class)
    public R2dbcTransactionManager connectionFactoryTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveSqlSessionFactory.class)
    public ReactiveSqlSessionFactory reactiveSqlSessionFactoryWithTransaction(R2dbcMybatisConfiguration configuration) {
        SpringReactiveMybatisExecutor springReactiveMybatisExecutor = new SpringReactiveMybatisExecutor(configuration);
        return DefaultReactiveSqlSessionFactory.newBuilder()
                .withR2dbcMybatisConfiguration(configuration)
                .withReactiveMybatisExecutor(springReactiveMybatisExecutor)
                .build();
    }

    /**
     * This will just scan the same base package as Spring Boot does. If you want more power, you can explicitly use
     * {@link R2dbcMapperScan} but this will get typed mappers working correctly, out-of-the-box,
     * similar to using Spring Data JPA repositories.
     */
    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {

        private BeanFactory beanFactory;
        private Environment environment;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {

            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                log.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }

            log.debug("Searching for mappers annotated with @Mapper");

            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            if (log.isDebugEnabled()) {
                packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
            }

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(R2dbcMapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue("annotationClass", Mapper.class);
            builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
            BeanWrapper beanWrapper = new BeanWrapperImpl(R2dbcMapperScannerConfigurer.class);
            Set<String> propertyNames = Stream.of(beanWrapper.getPropertyDescriptors()).map(PropertyDescriptor::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.contains("lazyInitialization")) {
                // Need to mybatis-spring 2.0.2+
                builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
            }
            if (propertyNames.contains("defaultScope")) {
                // Need to mybatis-spring 2.0.6+
                builder.addPropertyValue("defaultScope", "${mybatis.mapper-default-scope:}");
            }

            // for spring-native
            boolean injectSqlSession = environment.getProperty("mybatis.inject-sql-session-on-mapper-scan",
                    Boolean.class,
                    Boolean.TRUE
            );
            if (injectSqlSession && this.beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory listableBeanFactory = (ListableBeanFactory) this.beanFactory;
                Optional.ofNullable(getBeanNameForType(ReactiveSqlSessionFactory.class, listableBeanFactory))
                        .ifPresent(s -> builder.addPropertyValue("sqlSessionFactoryBeanName", s));
            }
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        private String getBeanNameForType(Class<?> type, ListableBeanFactory factory) {
            String[] beanNames = factory.getBeanNamesForType(type);
            return beanNames.length > 0 ? beanNames[0] : null;
        }

    }

    /**
     * If mapper registering configuration or mapper scanning configuration not present, this configuration allow to scan
     * mappers based on the same component-scanning path as Spring Boot itself.
     */
    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({R2dbcMapperFactoryBean.class, R2dbcMapperScannerConfigurer.class})
    public static class R2dbcMapperScannerRegistrarNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
            log.debug(
                    "Not found configuration for registering mapper bean using @R2dbcMapperScan, R2dbcMapperFactoryBean and R2dbcMapperScannerConfigurer.");
        }

    }
}

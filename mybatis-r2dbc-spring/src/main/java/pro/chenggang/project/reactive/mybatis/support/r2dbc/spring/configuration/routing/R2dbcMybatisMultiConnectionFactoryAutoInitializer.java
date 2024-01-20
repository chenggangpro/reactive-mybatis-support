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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.configuration.routing;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ValidationDepth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisRoutingConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.ConnectionFactoryOptionsCustomizer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

/**
 * The r2dbc mybatis multi connection factory bean auto initializer.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.r2dbc.mybatis.routing.enabled", havingValue = "true")
public class R2dbcMybatisMultiConnectionFactoryAutoInitializer implements ApplicationContextAware, InitializingBean {

    private R2dbcMybatisRoutingConnectionFactoryProperties r2dbcMybatisRoutingConnectionFactoryProperties;
    private ObjectProvider<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizerProvider;
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional<R2dbcMybatisConnectionFactoryProperties> optionalDefaultR2dbcMybatisConnectionFactoryProperties = r2dbcMybatisRoutingConnectionFactoryProperties.getDefinitions()
                .stream()
                .filter(R2dbcMybatisConnectionFactoryProperties::isAsDefault)
                .findFirst();
        if(!optionalDefaultR2dbcMybatisConnectionFactoryProperties.isPresent()){
            throw new IllegalStateException("When configuration using routing datasource , it should be set one to default at least , the property is : spring.r2dbc.mybatis.routing.definitions[?].as-default");
        }
        this.registerConnectionFactoryBean(optionalDefaultR2dbcMybatisConnectionFactoryProperties.get(),
                connectionFactoryOptionsCustomizerProvider,
                StringUtils.uncapitalize(ConnectionFactory.class.getSimpleName())
        );
        for (R2dbcMybatisConnectionFactoryProperties properties : this.r2dbcMybatisRoutingConnectionFactoryProperties.getDefinitions()) {
            this.registerConnectionFactoryBean(properties, connectionFactoryOptionsCustomizerProvider, null);
        }
    }


    /**
     * Register connection factory bean.
     *
     * @param r2dbcMybatisConnectionFactoryProperties    the r2dbc mybatis connection factory properties
     * @param connectionFactoryOptionsCustomizerProvider the connection factory options customizer provider
     * @param specificBeanName                           the specific bean name
     */
    protected void registerConnectionFactoryBean(R2dbcMybatisConnectionFactoryProperties r2dbcMybatisConnectionFactoryProperties,
                                                 ObjectProvider<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizerProvider,
                                                 String specificBeanName) {
        if (StringUtils.equals(r2dbcMybatisConnectionFactoryProperties.getName(), "defaultConnectionFactory")) {
            throw new IllegalStateException(
                    "When using Multi ConnectionFactory Routing, The ConnectionFactory defined in routing definitions ['defaultConnectionFactory' is not allowed](Url : "
                            + r2dbcMybatisConnectionFactoryProperties.getR2dbcUrl() + ")");
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ConnectionPool.class);
        ConnectionPoolConfiguration connectionPoolConfiguration = this.getConnectionPoolConfiguration(
                r2dbcMybatisConnectionFactoryProperties,
                connectionFactoryOptionsCustomizerProvider
        );
        beanDefinitionBuilder.addConstructorArgValue(connectionPoolConfiguration);
        beanDefinitionBuilder.setDestroyMethodName("dispose");
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) this.applicationContext.getBeanFactory();
        String beanName = StringUtils.defaultIfBlank(specificBeanName,
                r2dbcMybatisConnectionFactoryProperties.getName()
        );
        if (StringUtils.isBlank(beanName)) {
            throw new IllegalStateException(
                    "Using Multi ConnectionFactory Routing, The ConnectionFactory must be configured in properties (Url : " + r2dbcMybatisConnectionFactoryProperties.getR2dbcUrl() + ")");
        }
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }


    /**
     * Gets connection pool configuration.
     *
     * @param r2DbcMybatisConnectionFactoryProperties    the r2dbc mybatis connection factory properties
     * @param connectionFactoryOptionsCustomizerProvider the connection factory options customizer provider
     * @return the connection pool configuration
     */
    protected ConnectionPoolConfiguration getConnectionPoolConfiguration(R2dbcMybatisConnectionFactoryProperties r2DbcMybatisConnectionFactoryProperties,
                                                                         ObjectProvider<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizerProvider) {
        String determineConnectionFactoryUrl = r2DbcMybatisConnectionFactoryProperties.determineConnectionFactoryUrl();
        Assert.notNull(determineConnectionFactoryUrl, "R2DBC Connection URL must not be null");
        ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.parse(determineConnectionFactoryUrl);
        //ConnectionFactoryOptionsCustomizer
        if (Objects.nonNull(connectionFactoryOptionsCustomizerProvider)) {
            List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers = connectionFactoryOptionsCustomizerProvider
                    .orderedStream()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(connectionFactoryOptionsCustomizers)) {
                ConnectionFactoryOptions.Builder builder = connectionFactoryOptions.mutate();
                connectionFactoryOptionsCustomizers.forEach(
                        connectionFactoryOptionsCustomizer -> connectionFactoryOptionsCustomizer.customize(builder));
                connectionFactoryOptions = builder.build();
            }
        }
        ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
        R2dbcMybatisConnectionFactoryProperties.Pool pool = r2DbcMybatisConnectionFactoryProperties.getPool();
        ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
                .name(r2DbcMybatisConnectionFactoryProperties.determineConnectionFactoryName())
                .maxSize(pool.getMaxSize())
                .initialSize(pool.getInitialSize())
                .maxIdleTime(pool.getMaxIdleTime())
                .acquireRetry(pool.getAcquireRetry())
                .backgroundEvictionInterval(pool.getBackgroundEvictionInterval())
                .maxAcquireTime(pool.getMaxAcquireTime())
                .maxCreateConnectionTime(pool.getMaxCreateConnectionTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .validationDepth(pool.getValidationDepth());
        if (hasText(pool.getValidationQuery())) {
            builder.validationQuery(pool.getValidationQuery());
        } else {
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        return builder.build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        this.r2dbcMybatisRoutingConnectionFactoryProperties = applicationContext.getBean(
                R2dbcMybatisRoutingConnectionFactoryProperties.class);
        this.connectionFactoryOptionsCustomizerProvider = applicationContext.getBeanProvider(
                ConnectionFactoryOptionsCustomizer.class);
    }

}

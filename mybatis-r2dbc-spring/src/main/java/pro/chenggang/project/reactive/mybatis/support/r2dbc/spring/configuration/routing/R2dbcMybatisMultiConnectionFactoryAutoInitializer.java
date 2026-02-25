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
import io.r2dbc.spi.ConnectionFactoryOptions.Builder;
import io.r2dbc.spi.Option;
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
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.ConnectionPoolConfigurationCustomizer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private ObjectProvider<ConnectionPoolConfigurationCustomizer> connectionPoolConfigurationCustomizerProvider;
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional<R2dbcMybatisConnectionFactoryProperties> optionalDefaultR2dbcMybatisConnectionFactoryProperties = r2dbcMybatisRoutingConnectionFactoryProperties.getDefinitions()
                .stream()
                .filter(R2dbcMybatisConnectionFactoryProperties::isAsDefault)
                .findFirst();
        if (optionalDefaultR2dbcMybatisConnectionFactoryProperties.isEmpty()) {
            throw new IllegalStateException(
                    "When configuration using routing datasource , it should be set one to default at least , the property is : spring.r2dbc.mybatis.routing.definitions[?].as-default");
        }
        this.registerDefaultConnectionFactoryBean(optionalDefaultR2dbcMybatisConnectionFactoryProperties.get());
        for (R2dbcMybatisConnectionFactoryProperties properties : this.r2dbcMybatisRoutingConnectionFactoryProperties.getDefinitions()) {
            List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers = connectionFactoryOptionsCustomizerProvider.orderedStream()
                    .filter(customizer -> customizer.routingName().isEmpty() || customizer.routingName().get().equals(properties.getName()))
                    .toList();
            List<ConnectionPoolConfigurationCustomizer> connectionPoolConfigurationCustomizers = connectionPoolConfigurationCustomizerProvider.orderedStream()
                    .filter(customizer -> customizer.routingName().isEmpty() || customizer.routingName().get().equals(properties.getName()))
                    .toList();
            this.registerConnectionFactoryBean(properties, connectionFactoryOptionsCustomizers, connectionPoolConfigurationCustomizers, null);
        }
    }

    private void registerDefaultConnectionFactoryBean(R2dbcMybatisConnectionFactoryProperties defaultConnectionFactoryProperties) {
        List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers = connectionFactoryOptionsCustomizerProvider.orderedStream()
                .filter(customizer -> customizer.routingName().isEmpty() || customizer.routingName().get().equals(defaultConnectionFactoryProperties.getName()))
                .toList();
        List<ConnectionPoolConfigurationCustomizer> connectionPoolConfigurationCustomizers = connectionPoolConfigurationCustomizerProvider.orderedStream()
                .filter(customizer -> customizer.routingName().isEmpty() || customizer.routingName().get().equals(defaultConnectionFactoryProperties.getName()))
                .toList();
        this.registerConnectionFactoryBean(defaultConnectionFactoryProperties,
                connectionFactoryOptionsCustomizers,
                connectionPoolConfigurationCustomizers,
                StringUtils.uncapitalize(ConnectionFactory.class.getSimpleName())
        );
    }


    /**
     * Register connection factory bean.
     *
     * @param r2dbcMybatisConnectionFactoryProperties the r2dbc mybatis connection factory properties
     * @param connectionFactoryOptionsCustomizers     the connection factory options customizers
     * @param connectionPoolConfigurationCustomizers  the connection pool configuration customizers
     * @param specificBeanName                        the specific bean name
     */
    protected void registerConnectionFactoryBean(R2dbcMybatisConnectionFactoryProperties r2dbcMybatisConnectionFactoryProperties,
                                                 List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers,
                                                 List<ConnectionPoolConfigurationCustomizer> connectionPoolConfigurationCustomizers,
                                                 String specificBeanName) {
        if (StringUtils.equals(r2dbcMybatisConnectionFactoryProperties.getName(), "defaultConnectionFactory")) {
            throw new IllegalStateException(
                    "When using Multi ConnectionFactory Routing, The ConnectionFactory defined in routing definitions ['defaultConnectionFactory' is not allowed](Url : "
                            + r2dbcMybatisConnectionFactoryProperties.getR2dbcUrl() + ")");
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ConnectionPool.class);
        ConnectionPoolConfiguration connectionPoolConfiguration = this.getConnectionPoolConfiguration(
                r2dbcMybatisConnectionFactoryProperties,
                connectionFactoryOptionsCustomizers,
                connectionPoolConfigurationCustomizers
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
     * @param r2dbcMybatisConnectionFactoryProperties the r2dbc mybatis connection factory properties
     * @param connectionFactoryOptionsCustomizers     the connection factory options customizers
     * @param connectionPoolConfigurationCustomizers  the connection pool configuration customizers
     * @return the connection pool configuration
     */
    protected ConnectionPoolConfiguration getConnectionPoolConfiguration(R2dbcMybatisConnectionFactoryProperties r2dbcMybatisConnectionFactoryProperties,
                                                                         List<ConnectionFactoryOptionsCustomizer> connectionFactoryOptionsCustomizers,
                                                                         List<ConnectionPoolConfigurationCustomizer> connectionPoolConfigurationCustomizers) {
        String r2dbcUrl = r2dbcMybatisConnectionFactoryProperties.getR2dbcUrl();
        Assert.notNull(r2dbcUrl, "R2DBC Connection URL must not be null");
        Builder connectionFactoryOptionsBuilder = ConnectionFactoryOptions.parse(r2dbcUrl).mutate();
        if (Objects.nonNull(r2dbcMybatisConnectionFactoryProperties.getUsername())) {
            connectionFactoryOptionsBuilder.option(ConnectionFactoryOptions.USER, r2dbcMybatisConnectionFactoryProperties.getUsername());
        }
        if (Objects.nonNull(r2dbcMybatisConnectionFactoryProperties.getPassword())) {
            connectionFactoryOptionsBuilder.option(ConnectionFactoryOptions.PASSWORD, r2dbcMybatisConnectionFactoryProperties.getPassword());
        }
        if (!CollectionUtils.isEmpty(r2dbcMybatisConnectionFactoryProperties.getOptions())) {
            r2dbcMybatisConnectionFactoryProperties.getOptions()
                    .forEach((key, value) -> {
                        connectionFactoryOptionsBuilder.option(Option.valueOf(key), value);
                    });
        }
        //ConnectionFactoryOptionsCustomizer
        if (!CollectionUtils.isEmpty(connectionFactoryOptionsCustomizers)) {
            connectionFactoryOptionsCustomizers.forEach(customizer -> customizer.customize(connectionFactoryOptionsBuilder));
        }
        ConnectionFactoryOptions connectionFactoryOptions = connectionFactoryOptionsBuilder.build();
        ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
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
                .validationDepth(pool.getValidationDepth());
        if (hasText(pool.getValidationQuery())) {
            builder.validationQuery(pool.getValidationQuery());
        } else {
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        if (!CollectionUtils.isEmpty(connectionPoolConfigurationCustomizers)) {
            connectionPoolConfigurationCustomizers.forEach(customizer -> customizer.customize(builder));
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
        this.connectionPoolConfigurationCustomizerProvider = applicationContext.getBeanProvider(
                ConnectionPoolConfigurationCustomizer.class);
    }

}

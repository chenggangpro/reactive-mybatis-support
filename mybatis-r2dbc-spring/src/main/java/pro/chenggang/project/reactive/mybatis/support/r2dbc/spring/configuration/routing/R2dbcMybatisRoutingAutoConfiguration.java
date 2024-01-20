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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.configuration.MybatisLanguageDriverAutoConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor.SpringReactiveMybatisExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisRoutingConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.BeanNameDynamicRoutingConnectionFactoryLoader;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.DynamicRoutingConnectionFactoryLoader;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.R2dbcMybatisDynamicRoutingConnectionFactory;

/**
 * The r2dbc mybatis routing connection factory configuration
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({DataSourceAutoConfiguration.class, R2dbcAutoConfiguration.class, R2dbcTransactionManagerAutoConfiguration.class})
@AutoConfigureAfter({MybatisLanguageDriverAutoConfiguration.class})
@ConditionalOnProperty(value = "spring.r2dbc.mybatis.routing.enabled", havingValue = "true")
public class R2dbcMybatisRoutingAutoConfiguration {

    @ConfigurationProperties(prefix = R2dbcMybatisRoutingConnectionFactoryProperties.PREFIX)
    @Bean
    public R2dbcMybatisRoutingConnectionFactoryProperties r2dbcMybatisRoutingConnectionFactoryProperties() {
        return new R2dbcMybatisRoutingConnectionFactoryProperties();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicRoutingConnectionFactoryLoader.class)
    public DynamicRoutingConnectionFactoryLoader dynamicRoutingConnectionFactoryLoader() {
        log.debug(
                "No DynamicRoutingConnectionFactoryLoader Found, Use BeanNameDynamicRoutingConnectionFactoryLoader as default.");
        return new BeanNameDynamicRoutingConnectionFactoryLoader();
    }

    @Primary // configured as primary connection factory bean
    @Bean
    public R2dbcMybatisDynamicRoutingConnectionFactory r2dbcMybatisDynamicRoutingConnectionFactory(
            // Guarantee R2dbcMybatisMultiConnectionFactoryAutoInitializer always initialized before R2dbcMybatisDynamicRoutingConnectionFactory
            R2dbcMybatisMultiConnectionFactoryAutoInitializer r2dbcMybatisMultiConnectionFactoryAutoInitializer,
            DynamicRoutingConnectionFactoryLoader dynamicRoutingConnectionFactoryLoader) {
        return new R2dbcMybatisDynamicRoutingConnectionFactory(dynamicRoutingConnectionFactoryLoader);
    }

    @Primary // configured as primary reactive sql session factory
    @Bean
    public ReactiveSqlSessionFactory reactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration) {
        SpringReactiveMybatisExecutor springReactiveMybatisExecutor = new SpringReactiveMybatisExecutor(configuration);
        return DefaultReactiveSqlSessionFactory.newBuilder()
                .withR2dbcMybatisConfiguration(configuration)
                .withReactiveMybatisExecutor(springReactiveMybatisExecutor)
                .build();
    }

    @Primary // configured as primary r2dbc transaction manager
    @Bean
    public R2dbcTransactionManager r2dbcTransactionManager(R2dbcMybatisDynamicRoutingConnectionFactory r2dbcMybatisDynamicRoutingConnectionFactory) {
        return new R2dbcTransactionManager(r2dbcMybatisDynamicRoutingConnectionFactory);
    }

}

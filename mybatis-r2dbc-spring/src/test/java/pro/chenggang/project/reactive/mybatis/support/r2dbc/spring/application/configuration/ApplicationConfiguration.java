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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcDatabaseIdProvider;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.mapping.R2dbcVendorDatabaseIdProvider;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties.R2dbcMybatisProperties;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public R2dbcDatabaseIdProvider r2dbcDatabaseIdProvider(R2dbcMybatisProperties r2dbcMybatisProperties){
        R2dbcVendorDatabaseIdProvider r2dbcVendorDatabaseIdProvider = new R2dbcVendorDatabaseIdProvider();
        r2dbcVendorDatabaseIdProvider.setProperties(r2dbcMybatisProperties.getConfigurationProperties());
        return r2dbcVendorDatabaseIdProvider;
    }
}

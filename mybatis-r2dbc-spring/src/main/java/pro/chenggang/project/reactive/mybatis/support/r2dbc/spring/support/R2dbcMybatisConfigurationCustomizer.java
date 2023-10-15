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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * Callback interface that can be customized a {@link R2dbcMybatisConfiguration} object generated on auto-configuration.
 *
 * @author Gang Cheng
 * @since 1.0.4
 */
@FunctionalInterface
public interface R2dbcMybatisConfigurationCustomizer {

    /**
     * Customize the given a {@link R2dbcMybatisConfiguration} object.
     *
     * @param r2dbcMybatisConfiguration the R2dbcMybatisConfiguration object to customize
     */
    void customize(R2dbcMybatisConfiguration r2dbcMybatisConfiguration);

}

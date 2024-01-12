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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.ConnectionFactoryOptionsConfigurer;

import java.time.Duration;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public class MysqlConnectionFactoryOptionsConfigurer implements ConnectionFactoryOptionsConfigurer {

    @Override
    public void configure(ConnectionFactoryOptions.Builder optionsBuilder) {
        optionsBuilder.option(Option.valueOf("connectTimeout"), Duration.ofSeconds(10));
        optionsBuilder.option(Option.valueOf("sslMode"), "DISABLED");
    }
}

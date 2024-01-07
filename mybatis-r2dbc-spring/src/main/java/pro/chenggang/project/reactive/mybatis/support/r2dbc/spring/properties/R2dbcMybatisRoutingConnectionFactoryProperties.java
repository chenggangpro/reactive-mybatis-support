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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * The type R2dbc mybatis routing connection factory properties.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Getter
@Setter
@ToString
public class R2dbcMybatisRoutingConnectionFactoryProperties {

    public static final String PREFIX = "spring.r2dbc.mybatis.routing";

    /**
     * Whether enable the routing connection factory configuration
     */
    private Boolean enabled = false;

    /**
     * The r2dbc mybatis connection factory properties definitions
     */
    private List<R2dbcMybatisConnectionFactoryProperties> definitions = new ArrayList<>();
}

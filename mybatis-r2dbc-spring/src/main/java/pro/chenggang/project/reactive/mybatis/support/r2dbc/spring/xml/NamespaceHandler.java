/**
 * Copyright 2010-2019 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for the MyBatis namespace.
 *
 * @author Lishu Luo
 *
 * @see R2dbcMapperScannerBeanDefinitionParser
 * @since 1.2.0
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        registerBeanDefinitionParser("scan", new R2dbcMapperScannerBeanDefinitionParser());
    }

}

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
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcXMLConfigBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

import static org.springframework.util.Assert.state;

/**
 * r2dbc mybatis properties
 *
 * @author Gang Cheng
 * @version 1.0.4
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class R2dbcMybatisProperties implements InitializingBean {

    /**
     * The R2dbcMybatisProperties PREFIX.
     */
    public static final String PREFIX = "r2dbc.mybatis";

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * Mybatis config file's location
     */
    private String configLocation;

    /**
     * Locations of MyBatis mapper files.
     */
    private String[] mapperLocations;

    /**
     * specific type alias classes
     */
    private Class<?>[] typeAliases;

    /**
     * Packages to search type aliases. (Package delimiters are ",; \t\n")
     */
    private String typeAliasesPackage;

    /**
     * The super class for filtering type alias. If this not specifies, the MyBatis deal as type alias all classes that
     * searched from typeAliasesPackage.
     */
    private Class<?> typeAliasesSuperType;

    /**
     * Packages to search for type handlers. (Package delimiters are ",; \t\n")
     */
    private String typeHandlersPackage;

    /**
     * Packages to search for R2dbc TypeHandler Adapter. (Package delimiters are ",; \t\n")
     */
    private String r2dbcTypeHandlerAdapterPackage;

    /**
     * The default scripting language driver class. (Available when use together with mybatis-spring 2.0.2+)
     */
    private Class<? extends LanguageDriver> defaultScriptingLanguageDriver;

    /**
     * Externalized properties for MyBatis configuration.
     */
    private Properties configurationProperties;

    /**
     * The default Enum TypeHandler class
     */
    private Class<? extends TypeHandler<?>> defaultEnumTypeHandler;

    /**
     * A Configuration object for customize default settings.
     */
    @NestedConfigurationProperty
    private R2dbcMybatisConfiguration configuration;

    public Resource[] resolveMapperLocations() {
        //#149
        if (this.mapperLocations == null) {
            return null;
        }
        return Stream.of(this.mapperLocations)
                .flatMap(location -> Stream.of(getResources(location)))
                .toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null),
                "Property 'configuration' and 'configLocation' can not specified with together"
        );
        if (this.configLocation != null) {
            Resource[] configResources = this.getResources(this.configLocation);
            if (configResources.length == 0) {
                throw new IllegalStateException("Could not find mybatis config file from location : " + this.configLocation);
            }
            R2dbcXMLConfigBuilder r2dbcXMLConfigBuilder = new R2dbcXMLConfigBuilder(configResources[0].getInputStream(),
                    null,
                    this.configurationProperties
            );
            this.configuration = r2dbcXMLConfigBuilder.parse();
        }
    }
}

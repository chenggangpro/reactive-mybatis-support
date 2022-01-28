package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

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
public class R2dbcMybatisProperties {

    /**
     * The R2dbcMybatisProperties PREFIX.
     */
    public static final String PREFIX = "r2dbc.mybatis";

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

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
        return Stream.of(Optional.ofNullable(this.mapperLocations).orElse(new String[0]))
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }
}

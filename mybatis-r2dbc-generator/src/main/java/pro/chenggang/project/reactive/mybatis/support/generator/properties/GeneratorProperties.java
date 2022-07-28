package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.option.LombokConfig;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.GeneratedJavaTypeModifier;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.DYNAMIC;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.DYNAMIC_MAPPER;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.MODEL;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.MODEL_XML;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.SIMPLE;

/**
 * The Generator properties.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorProperties {

    /**
     * The target generator types
     */
    private Set<GeneratorType> generatorTypes;

    /**
     * Whether extend dynamic mapper
     */
    private boolean extendDynamicMapper = true;

    /**
     * Whether overwrite generated file
     */
    private boolean overwrite = false;

    /**
     * Whether generate returned key
     */
    private boolean generateReturnedKey = false;

    /**
     * Whether generate column comment
     */
    private boolean generateComment = true;

    /**
     * The table name trim regex pattern
     * Setting '^Sys' will replace the generated table name start with Sys
     * available when table name is specific
     */
    private String tableNameTrimPattern;

    /**
     * The column name trim regex pattern
     * Setting '^Sys' will replace the generated column name start with Sys
     * available when table name is specific
     */
    private String columnNameTrimPattern;

    /**
     * The default java type modifier class
     */
    private Class<? extends GeneratedJavaTypeModifier> defaultJavaTypeModifierClass;

    /**
     * The target location
     */
    private TargetLocation targetLocation;

    /**
     * The target package
     */
    private TargetPackage targetPackage;

    /**
     * The target connection
     */
    private TargetConnection targetConnection;

    /**
     * The Lombok configs
     */
    private Set<LombokConfig> lombokConfigs;

    /**
     * The target table names,if empty then generate all tables
     */
    private Set<String> tableNames;

    /**
     * Validate
     */
    public void validate() {
        if (Objects.isNull(generatorTypes) || generatorTypes.isEmpty()) {
            throw new IllegalArgumentException("Generator types should be configured");
        }
        Objects.requireNonNull(targetLocation, () -> "Target location should be configured");
        Objects.requireNonNull(targetPackage, () -> "Target package should be configured");
        Objects.requireNonNull(targetConnection, () -> "Target connection should be configured");
        this.requiredNotBlank(targetLocation.javaLocation, () -> "Java location should be configured");
        this.requiredNotBlank(targetLocation.getJavaLocation(), () -> "Java location should be configured");
        this.requiredNotBlank(targetLocation.getMapperXmlLocation(), () -> "Mapper xml location should be configured");
        this.requiredNotBlank(targetPackage.getModelPackage(), () -> "Model package should be configured");
        this.requiredNotBlank(targetPackage.getMapperInterfacePackage(), () -> "Mapper interface package should be configured");
        this.requiredNotBlank(targetPackage.getMapperXmlPackage(), () -> "Mapper xml package should be configured");
        this.requiredNotBlank(targetConnection.getJdbcConnectionUrl(), () -> "JDBC connection URL should be configured");
        this.requiredNotBlank(targetConnection.getJdbcDriverClassName(), () -> "JDBC driver class name should be configured");
        this.requiredNotBlank(targetConnection.getPassword(), () -> "JDBC password should be configured");
        this.requiredNotBlank(targetConnection.getUsername(), () -> "JDBC username should be configured");
        if (generatorTypes.contains(DYNAMIC)) {
            generatorTypes.remove(MODEL);
            generatorTypes.remove(MODEL_XML);
            generatorTypes.remove(DYNAMIC_MAPPER);
        }
        if (generatorTypes.contains(SIMPLE)) {
            generatorTypes.remove(MODEL);
            generatorTypes.remove(MODEL_XML);
        }
    }

    /**
     * required not blank
     *
     * @param value                the value
     * @param errorMessageSupplier the error message
     */
    private void requiredNotBlank(String value, Supplier<String> errorMessageSupplier) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(errorMessageSupplier.get());
        }
    }

    /**
     * The Target location.
     */
    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetLocation {

        /**
         * The base location
         * FYI: the whole project's location (Absolute Dir)
         */
        private String baseLocation;

        /**
         * The java location
         * FYI: the project's java location (src/main/java)
         */
        private String javaLocation;

        /**
         * The mapper xml location
         * FYI: the project's mapper xml location (src/main/resources)
         */
        private String mapperXmlLocation;

        /**
         * Get full java location.
         *
         * @return the full java location
         */
        public String getFullJavaLocation() {
            if (StringUtils.isNotBlank(baseLocation) && StringUtils.endsWith(baseLocation, "/")) {
                this.baseLocation = StringUtils.substringBeforeLast(baseLocation, "/");
            }
            return Stream.of(baseLocation, javaLocation)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("/"));
        }

        /**
         * Get full mapper xml location.
         *
         * @return the full mapper xml location
         */
        public String getFullMapperXmlLocation() {
            if (StringUtils.isNotBlank(baseLocation) && StringUtils.endsWith(baseLocation, "/")) {
                this.baseLocation = StringUtils.substringBeforeLast(baseLocation, "/");
            }
            return Stream.of(baseLocation, mapperXmlLocation)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("/"));
        }

    }

    /**
     * The Target package.
     */
    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetPackage {

        /**
         * The base package
         * FYI: the whole project's common package
         */
        private String basePackage;

        /**
         * The model package
         * FYI: if basePackage is blank, this should be the full package name
         */
        private String modelPackage;

        /**
         * The mapper interface package
         * FYI: if basePackage is blank, this should be the full package name
         */
        private String mapperInterfacePackage;

        /**
         * The mapper xml package
         * FYI: the xml directory in mapper xml location
         */
        private String mapperXmlPackage;

        /**
         * Get full model package.
         *
         * @return the full model package
         */
        public String getFullModelPackage() {
            return Stream.of(basePackage, modelPackage)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("."));
        }

        /**
         * Get full mapper interface package.
         *
         * @return the full mapper interface package
         */
        public String getFullMapperInterfacePackage() {
            return Stream.of(basePackage, mapperInterfacePackage)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("."));
        }

    }

    /**
     * The Target connection.
     */
    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetConnection {

        /**
         * The jdbc driver class name
         */
        private String jdbcDriverClassName;

        /**
         * The jdbc connection url
         */
        private String jdbcConnectionUrl;

        /**
         * The jdbc username
         */
        private String username;

        /**
         * The jdbc password
         */
        private String password;

    }

}

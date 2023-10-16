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
package pro.chenggang.project.reactive.mybatis.support.generator.properties;

import org.mybatis.generator.api.dom.java.JavaVisibility;
import pro.chenggang.project.reactive.mybatis.support.generator.core.MybatisDynamicCodeGenerator.Configurer;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.GeneratedJavaTypeModifier;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties.TargetConnection;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties.TargetLocation;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties.TargetPackage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Generator properties builder
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class GeneratorPropertiesBuilder {

    private final Configurer configurer;
    private Set<GeneratorType> generatorTypes = new HashSet<>();
    private boolean extendDynamicMapper = true;
    private boolean overwrite = false;
    private boolean generateReturnedKey = false;
    private boolean generateComment = true;
    private String tableNameTrimPattern;
    private String columnNameTrimPattern;
    private Class<? extends GeneratedJavaTypeModifier> generatedJavaTypeModifierClass;
    private Set<String> lombokConfigs = new HashSet<>();
    private Set<String> tableNames;
    private boolean finalField = false;
    private JavaVisibility fieldVisibility = JavaVisibility.PROTECTED;
    private TargetLocation targetLocation;
    private TargetPackage targetPackage;
    private TargetConnection targetConnection;

    protected GeneratorPropertiesBuilder(Configurer configurer) {
        this.configurer = configurer;
    }

    public GeneratorPropertiesBuilder(GeneratorProperties generatorProperties, Configurer configurer) {
        this.configurer = configurer;
        this.generatorTypes = generatorProperties.getGeneratorTypes();
        this.extendDynamicMapper = generatorProperties.isExtendDynamicMapper();
        this.overwrite = generatorProperties.isOverwrite();
        this.generateReturnedKey = generatorProperties.isGenerateReturnedKey();
        this.generateComment = generatorProperties.isGenerateComment();
        this.finalField = generatorProperties.isFinalField();
        this.fieldVisibility = generatorProperties.getFieldVisibility();
        this.tableNameTrimPattern = generatorProperties.getTableNameTrimPattern();
        this.columnNameTrimPattern = generatorProperties.getColumnNameTrimPattern();
        this.generatedJavaTypeModifierClass = generatorProperties.getGeneratedJavaTypeModifierClass();
        this.lombokConfigs = generatorProperties.getLombokAnnotations();
        this.tableNames = generatorProperties.getTableNames();
        this.targetLocation = generatorProperties.getTargetLocation();
        this.targetPackage = generatorProperties.getTargetPackage();
        this.targetConnection = generatorProperties.getTargetConnection();
    }

    /**
     * The target generator types
     *
     * @param generatorTypes the generator types
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder generatorTypes(GeneratorType... generatorTypes) {
        this.generatorTypes.addAll(Arrays.asList(generatorTypes));
        return this;
    }

    /**
     * The target generator types
     *
     * @param generatorTypes the generator types
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder generatorTypes(Set<GeneratorType> generatorTypes) {
        this.generatorTypes = generatorTypes;
        return this;
    }

    /**
     * Whether extend dynamic mapper
     *
     * @param extendDynamicMapper extend dynamic mapper or not
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder extendDynamicMapper(boolean extendDynamicMapper) {
        this.extendDynamicMapper = extendDynamicMapper;
        return this;
    }

    /**
     * Whether overwrite generated file
     *
     * @param overwrite overwrite or not
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    /**
     * Whether generate returned key
     *
     * @param generateReturnedKey generate returned key or not
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder generateReturnedKey(boolean generateReturnedKey) {
        this.generateReturnedKey = generateReturnedKey;
        return this;
    }

    /**
     * Whether generate column comment
     *
     * @param generateComment generate comment or not
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder generateComment(boolean generateComment) {
        this.generateComment = generateComment;
        return this;
    }

    /**
     * Set generated class's field to be final.
     *
     * @param finalField the final field
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder finalField(boolean finalField) {
        this.finalField = finalField;
        return this;
    }

    /**
     * Set field visibility
     *
     * @param fieldVisibility the field visibility
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder fieldVisibility(JavaVisibility fieldVisibility) {
        this.fieldVisibility = fieldVisibility;
        return this;
    }

    /**
     * The table name trim regex pattern
     * Setting '^Sys' will replace the generated table name start with Sys
     * available when table name is specific
     *
     * @param tableNameTrimPattern the table name trim pattern
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder tableNameTrimPattern(String tableNameTrimPattern) {
        this.tableNameTrimPattern = tableNameTrimPattern;
        return this;
    }

    /**
     * The column name trim regex pattern
     * Setting '^Sys' will replace the generated column name start with Sys
     * available when table name is specific
     *
     * @param columnNameTrimPattern the column name trim pattern
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder columnNameTrimPattern(String columnNameTrimPattern) {
        this.columnNameTrimPattern = columnNameTrimPattern;
        return this;
    }

    /**
     * The generated java type modifier class
     *
     * @param generatedJavaTypeModifierClass the generated java type modifier class
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder generatedJavaTypeModifierClass(Class<? extends GeneratedJavaTypeModifier> generatedJavaTypeModifierClass) {
        this.generatedJavaTypeModifierClass = generatedJavaTypeModifierClass;
        return this;
    }

    /**
     * The lombok annotations
     *
     * @param lombokConfigs The lombok annotations
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder lombokConfigs(String... lombokConfigs) {
        this.lombokConfigs.addAll(Arrays.asList(lombokConfigs));
        return this;
    }

    /**
     * The lombok annotations
     *
     * @param lombokConfigs The lombok annotations
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder lombokConfigs(Set<String> lombokConfigs) {
        this.lombokConfigs = lombokConfigs;
        return this;
    }

    /**
     * The target table names,if empty then generate all tables
     *
     * @param tableNames the table names
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder tableNames(String... tableNames) {
        this.tableNames.addAll(Arrays.asList(tableNames));
        return this;
    }

    /**
     * The target table names,if empty then generate all tables
     *
     * @param tableNames the table names
     * @return the generator properties builder
     */
    public GeneratorPropertiesBuilder tableNames(Set<String> tableNames) {
        this.tableNames = tableNames;
        return this;
    }

    /**
     * new target location builder
     *
     * @return the target location builder
     */
    public TargetLocationBuilder targetLocationBuilder() {
        return new TargetLocationBuilder(this);
    }

    /**
     * new target package builder
     *
     * @return the target package builder
     */
    public TargetPackageBuilder targetPackageBuilder() {
        return new TargetPackageBuilder(this);
    }

    /**
     * new target connection builder
     *
     * @return the target connection builder
     */
    public TargetConnectionBuilder targetConnectionBuilder() {
        return new TargetConnectionBuilder(this);
    }

    /**
     * Build GeneratorProperties
     *
     * @return the GeneratorProperties
     */
    protected GeneratorProperties build() {
        return new GeneratorProperties(generatorTypes,
                extendDynamicMapper,
                overwrite,
                generateReturnedKey,
                generateComment,
                finalField,
                fieldVisibility,
                tableNameTrimPattern,
                columnNameTrimPattern,
                generatedJavaTypeModifierClass,
                targetLocation,
                targetPackage,
                targetConnection,
                lombokConfigs,
                tableNames
        );
    }

    /**
     * Then return Configure
     *
     * @return the configurer
     */
    public Configurer thenConfigurer() {
        GeneratorPropertiesHolder.getInstance().setGeneratorPropertiesLoader(new FluentGeneratorPropertiesLoader(this));
        return this.configurer;
    }

    /**
     * The target location builder
     */
    public static class TargetLocationBuilder {

        private final GeneratorPropertiesBuilder generatorPropertiesBuilder;

        private String baseLocation;
        private String javaLocation = "src/main/java";
        private String mapperXmlLocation = "src/main/resources";

        private TargetLocationBuilder(GeneratorPropertiesBuilder generatorPropertiesBuilder) {
            this.generatorPropertiesBuilder = generatorPropertiesBuilder;
            TargetLocation targetLocation = generatorPropertiesBuilder.targetLocation;
            if (Objects.nonNull(targetLocation)) {
                this.baseLocation = targetLocation.getBaseLocation();
                this.javaLocation = targetLocation.getJavaLocation();
                this.mapperXmlLocation = targetLocation.getMapperXmlLocation();
            }
        }

        /**
         * Base location.
         * FYI: the whole project's location (Absolute Dir)
         *
         * @param baseLocation the base location
         * @return the target location builder
         */
        public TargetLocationBuilder baseLocation(String baseLocation) {
            this.baseLocation = baseLocation;
            return this;
        }

        /**
         * Java location.
         * FYI: the project's java location (src/main/java)
         *
         * @param javaLocation the java location
         * @return the target location builder
         */
        public TargetLocationBuilder javaLocation(String javaLocation) {
            this.javaLocation = javaLocation;
            return this;
        }

        /**
         * Mapper xml location.
         * FYI: the project's mapper xml location (src/main/resources)
         *
         * @param mapperXmlLocation the mapper xml location
         * @return the target location builder
         */
        public TargetLocationBuilder mapperXmlLocation(String mapperXmlLocation) {
            this.mapperXmlLocation = mapperXmlLocation;
            return this;
        }

        /**
         * Build TargetLocation and return GeneratorPropertiesBuilder
         *
         * @return the GeneratorPropertiesBuilder
         */
        public GeneratorPropertiesBuilder thenBuilder() {
            generatorPropertiesBuilder.targetLocation = new TargetLocation(this.baseLocation, this.javaLocation, mapperXmlLocation);
            return this.generatorPropertiesBuilder;
        }
    }

    /**
     * The target package builder.
     */
    public static class TargetPackageBuilder {

        private final GeneratorPropertiesBuilder generatorPropertiesBuilder;

        private String basePackage;
        private String modelPackage;
        private String mapperInterfacePackage;
        private String mapperXmlPackage;

        private TargetPackageBuilder(GeneratorPropertiesBuilder generatorPropertiesBuilder) {
            this.generatorPropertiesBuilder = generatorPropertiesBuilder;
            TargetPackage targetPackage = generatorPropertiesBuilder.targetPackage;
            if (Objects.nonNull(targetPackage)) {
                this.basePackage = targetPackage.getBasePackage();
                this.modelPackage = targetPackage.getModelPackage();
                this.mapperInterfacePackage = targetPackage.getMapperInterfacePackage();
                this.mapperXmlPackage = targetPackage.getMapperXmlPackage();
            }
        }

        /**
         * Base package.
         * FYI: the whole project's common package
         *
         * @param basePackage the base package
         * @return the target package builder
         */
        public TargetPackageBuilder basePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }

        /**
         * Model package.
         * FYI: if basePackage is blank, this should be the full package name
         *
         * @param modelPackage the model package
         * @return the target package builder
         */
        public TargetPackageBuilder modelPackage(String modelPackage) {
            this.modelPackage = modelPackage;
            return this;
        }

        /**
         * Mapper interface package.
         * FYI: if basePackage is blank, this should be the full package name
         *
         * @param mapperInterfacePackage the mapper interface package
         * @return the target package builder
         */
        public TargetPackageBuilder mapperInterfacePackage(String mapperInterfacePackage) {
            this.mapperInterfacePackage = mapperInterfacePackage;
            return this;
        }

        /**
         * Mapper xml package.
         * FYI: the xml directory in mapper xml location
         *
         * @param mapperXmlPackage the mapper xml package
         * @return the target package builder
         */
        public TargetPackageBuilder mapperXmlPackage(String mapperXmlPackage) {
            this.mapperXmlPackage = mapperXmlPackage;
            return this;
        }

        /**
         * Build TargetPackage and return GeneratorPropertiesBuilder
         *
         * @return the GeneratorPropertiesBuilder
         */
        public GeneratorPropertiesBuilder thenPropertiesBuilder() {
            generatorPropertiesBuilder.targetPackage = new TargetPackage(this.basePackage, this.modelPackage, this.mapperInterfacePackage, this.mapperXmlPackage);
            return this.generatorPropertiesBuilder;
        }
    }

    /**
     * The target connection builder.
     */
    public static class TargetConnectionBuilder {

        private final GeneratorPropertiesBuilder generatorPropertiesBuilder;

        private String jdbcDriverClassName;
        private String jdbcConnectionUrl;
        private String username;
        private String password;

        private TargetConnectionBuilder(GeneratorPropertiesBuilder generatorPropertiesBuilder) {
            this.generatorPropertiesBuilder = generatorPropertiesBuilder;
            TargetConnection targetConnection = generatorPropertiesBuilder.targetConnection;
            if (Objects.nonNull(targetConnection)) {
                this.jdbcDriverClassName = targetConnection.getJdbcDriverClassName();
                this.jdbcConnectionUrl = targetConnection.getJdbcConnectionUrl();
                this.username = targetConnection.getUsername();
                this.password = targetConnection.getPassword();
            }
        }

        /**
         * The Jdbc driver class name.
         *
         * @param jdbcDriverClassName the jdbc driver class name
         * @return the target connection builder
         */
        public TargetConnectionBuilder jdbcDriverClassName(String jdbcDriverClassName) {
            this.jdbcDriverClassName = jdbcDriverClassName;
            return this;
        }

        /**
         * The Jdbc connection url.
         *
         * @param jdbcConnectionUrl the jdbc connection url
         * @return the target connection builder
         */
        public TargetConnectionBuilder jdbcConnectionUrl(String jdbcConnectionUrl) {
            this.jdbcConnectionUrl = jdbcConnectionUrl;
            return this;
        }

        /**
         * The jdbc username.
         *
         * @param username the username
         * @return the target connection builder
         */
        public TargetConnectionBuilder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * The jdbc password.
         *
         * @param password the password
         * @return the target connection builder
         */
        public TargetConnectionBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Build TargetConnection and return GeneratorPropertiesBuilder
         *
         * @return the GeneratorPropertiesBuilder
         */
        public GeneratorPropertiesBuilder thenBuilder() {
            generatorPropertiesBuilder.targetConnection = new TargetConnection(this.jdbcDriverClassName, this.jdbcConnectionUrl, this.username, this.password);
            return this.generatorPropertiesBuilder;
        }
    }
}
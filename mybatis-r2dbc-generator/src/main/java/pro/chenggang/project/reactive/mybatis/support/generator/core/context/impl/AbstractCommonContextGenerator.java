package pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.config.ColumnRenamingRule;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.DomainObjectRenamingRule;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.JavaTypeResolverConfiguration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.ContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.comment.CustomCommentGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.generator.CustomGeneratorPlugin;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.CustomJavaTypeResolver;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;

import java.util.Objects;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractCommonContextGenerator implements ContextGenerator {

    /**
     * new context
     *
     * @return the context
     */
    protected abstract Context newContext();

    @Override
    public Context generateContext(GeneratorProperties generatorProperties) {
        Context context = newContext();
        determineGenerateComment(context, generatorProperties);
        configureGeneratorPlugin(context, generatorProperties);
        configureJDBCConnection(context, generatorProperties);
        configureModelGenerator(context, generatorProperties);
        configureSqlMapGenerator(context, generatorProperties);
        configureJavaClientGenerator(context, generatorProperties);
        configureTable(context, generatorProperties);
        configureJavaType(context, generatorProperties);
        return context;
    }

    /**
     * determine generate comment
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void determineGenerateComment(Context context, GeneratorProperties generatorProperties) {
        if (!generatorProperties.isGenerateComment()) {
            return;
        }
        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
        commentGeneratorConfiguration.setConfigurationType(CustomCommentGenerator.class.getCanonicalName());
        context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);
    }

    /**
     * add custom generator plugin
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureGeneratorPlugin(Context context, GeneratorProperties generatorProperties) {
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType(CustomGeneratorPlugin.class.getCanonicalName());
        pluginConfiguration.addProperty("extendDynamicMapper", String.valueOf(generatorProperties.isExtendDynamicMapper()));
        context.addPluginConfiguration(pluginConfiguration);
        PluginConfiguration unMergeablePlugin = new PluginConfiguration();
        unMergeablePlugin.setConfigurationType(UnmergeableXmlMappersPlugin.class.getCanonicalName());
        context.addPluginConfiguration(unMergeablePlugin);
    }

    /**
     * configure jdbc connection
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureJDBCConnection(Context context, GeneratorProperties generatorProperties) {
        GeneratorProperties.TargetConnection targetConnection = generatorProperties.getTargetConnection();
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(targetConnection.getJdbcConnectionUrl());
        jdbcConnectionConfiguration.setDriverClass(targetConnection.getJdbcDriverClassName());
        jdbcConnectionConfiguration.setUserId(targetConnection.getUsername());
        jdbcConnectionConfiguration.setPassword(targetConnection.getPassword());
        jdbcConnectionConfiguration.addProperty("nullCatalogMeansCurrent", "true");
        if (generatorProperties.isGenerateComment()) {
            jdbcConnectionConfiguration.addProperty("remarksReporting", "true");
            jdbcConnectionConfiguration.addProperty("useInformationSchema", "true");
        }
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);
    }

    /**
     * configure model generator
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureModelGenerator(Context context, GeneratorProperties generatorProperties) {
        GeneratorProperties.TargetLocation targetLocation = generatorProperties.getTargetLocation();
        GeneratorProperties.TargetPackage targetPackage = generatorProperties.getTargetPackage();
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(targetPackage.getFullModelPackage());
        javaModelGeneratorConfiguration.setTargetProject(targetLocation.getFullJavaLocation());
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
    }

    /**
     * configure sql map generator
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureSqlMapGenerator(Context context, GeneratorProperties generatorProperties) {
        GeneratorProperties.TargetLocation targetLocation = generatorProperties.getTargetLocation();
        GeneratorProperties.TargetPackage targetPackage = generatorProperties.getTargetPackage();
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage(targetPackage.getMapperXmlPackage());
        sqlMapGeneratorConfiguration.setTargetProject(targetLocation.getFullMapperXmlLocation());
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);
    }

    /**
     * configure java client generator
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureJavaClientGenerator(Context context, GeneratorProperties generatorProperties) {
        GeneratorProperties.TargetLocation targetLocation = generatorProperties.getTargetLocation();
        GeneratorProperties.TargetPackage targetPackage = generatorProperties.getTargetPackage();
        JavaClientGeneratorConfiguration clientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        clientGeneratorConfiguration.setTargetPackage(targetPackage.getFullMapperInterfacePackage());
        clientGeneratorConfiguration.setTargetProject(targetLocation.getFullJavaLocation());
        clientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(clientGeneratorConfiguration);
    }

    /**
     * configure table generator
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureTable(Context context, GeneratorProperties generatorProperties) {
        String tableNameTrimPattern = generatorProperties.getTableNameTrimPattern();
        boolean shouldTrimTableName = StringUtils.isNotBlank(tableNameTrimPattern);
        String columnNameTrimPattern = generatorProperties.getColumnNameTrimPattern();
        boolean shouldTrimColumnName = StringUtils.isNotBlank(columnNameTrimPattern);
        generatorProperties.getTableNames()
                .stream()
                .map(item -> {
                            TableConfiguration tableConfiguration = new TableConfiguration(context);
                            tableConfiguration.setTableName(item);
                            if (shouldTrimTableName) {
                                DomainObjectRenamingRule domainObjectRenamingRule = new DomainObjectRenamingRule();
                                domainObjectRenamingRule.setSearchString(tableNameTrimPattern);
                                domainObjectRenamingRule.setReplaceString("");
                                tableConfiguration.setDomainObjectRenamingRule(domainObjectRenamingRule);
                            }
                            if (shouldTrimColumnName) {
                                ColumnRenamingRule columnRenamingRule = new ColumnRenamingRule();
                                columnRenamingRule.setSearchString(columnNameTrimPattern);
                                columnRenamingRule.setReplaceString("");
                                tableConfiguration.setColumnRenamingRule(columnRenamingRule);
                            }
                            return tableConfiguration;
                        }
                )
                .forEach(context::addTableConfiguration);
    }

    /**
     * configure java type
     *
     * @param context             the Context
     * @param generatorProperties the generator properties
     */
    protected void configureJavaType(Context context, GeneratorProperties generatorProperties) {
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
        javaTypeResolverConfiguration.setConfigurationType(CustomJavaTypeResolver.class.getCanonicalName());
        javaTypeResolverConfiguration.addProperty("useJSR310Types", "true");
        javaTypeResolverConfiguration.addProperty("forceBigDecimals", "true");
        if (Objects.nonNull(generatorProperties.getDefaultJavaTypeModifierClass())) {
            javaTypeResolverConfiguration.addProperty("defaultJavaTypeModifierType", generatorProperties.getDefaultJavaTypeModifierClass().getCanonicalName());
        }
        context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);
    }
}

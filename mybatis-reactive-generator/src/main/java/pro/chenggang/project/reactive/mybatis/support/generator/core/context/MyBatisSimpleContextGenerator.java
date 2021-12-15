package pro.chenggang.project.reactive.mybatis.support.generator.core.context;

import org.mybatis.generator.config.ColumnRenamingRule;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.DomainObjectRenamingRule;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.JavaTypeResolverConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.StringUtility;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.support.ExtensionCommentGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorExtensionProperties;

/**
 * @author: chenggang
 * @date 2019-10-22.
 */
public class MyBatisSimpleContextGenerator implements ContextGenerator {

    @Override
    public GeneratorType targetGeneratorType() {
        return GeneratorType.SIMPLE;
    }

    @Override
    public Context generateContext(GeneratorExtensionProperties extensionProperties) {
        String basePackage = extensionProperties.getBasePackage();
        Context basicContext = new Context(ModelType.FLAT);
        basicContext.setTargetRuntime("MyBatis3Simple");
        basicContext.setId("MyBatis3Simple");
        basicContext.addProperty("javaFileEncoding","UTF-8");
        basicContext.addProperty("columnOverride","false");
        if (extensionProperties.isGenerateComment()) {
            CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
            commentGeneratorConfiguration.setConfigurationType(ExtensionCommentGenerator.class.getCanonicalName());
            basicContext.setCommentGeneratorConfiguration(commentGeneratorConfiguration);
        }
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("pro.chenggang.project.reactive.mybatis.support.generator.support.GenerateExtensionPlugin");
        pluginConfiguration.addProperty("extendDynamicMapper", String.valueOf(extensionProperties.isExtendDynamicMapper()));
        basicContext.addPluginConfiguration(pluginConfiguration);
        PluginConfiguration unmergeablePlugin = new PluginConfiguration();
        unmergeablePlugin.setConfigurationType("org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin");
        basicContext.addPluginConfiguration(unmergeablePlugin);
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(extensionProperties.getConnectionUrl());
        jdbcConnectionConfiguration.setDriverClass(extensionProperties.getDriverClass());
        jdbcConnectionConfiguration.setUserId(extensionProperties.getUsername());
        jdbcConnectionConfiguration.setPassword(extensionProperties.getPassword());
        jdbcConnectionConfiguration.addProperty("nullCatalogMeansCurrent","true");
        if (extensionProperties.isGenerateComment()) {
            jdbcConnectionConfiguration.addProperty("remarksReporting", "true");
            jdbcConnectionConfiguration.addProperty("useInformationSchema", "true");
        }
        basicContext.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(basePackage + "." + extensionProperties.getModelPackage());
        javaModelGeneratorConfiguration.setTargetProject(extensionProperties.getParentLocation()+extensionProperties.getJavaLocation());
        basicContext.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage(extensionProperties.getMapperXmPackage());
        sqlMapGeneratorConfiguration.setTargetProject(extensionProperties.getParentLocation()+extensionProperties.getMapperXmlLocation());
        basicContext.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);
        JavaClientGeneratorConfiguration clientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        clientGeneratorConfiguration.setTargetPackage(basePackage + "." + extensionProperties.getMapperInterfacePackage());
        clientGeneratorConfiguration.setTargetProject(extensionProperties.getParentLocation()+extensionProperties.getJavaLocation());
        clientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        basicContext.setJavaClientGeneratorConfiguration(clientGeneratorConfiguration);
        String trimPattern = extensionProperties.getTableTrimPattern();
        boolean shouldTrimTableName = StringUtility.stringHasValue(trimPattern);
        extensionProperties.getTableName().stream().map(item->{
            TableConfiguration tableConfiguration = new TableConfiguration(basicContext);
            tableConfiguration.setTableName(item);
            if(shouldTrimTableName){
                ColumnRenamingRule columnRenamingRule = new ColumnRenamingRule();
                columnRenamingRule.setSearchString(trimPattern);
                columnRenamingRule.setReplaceString("");
                DomainObjectRenamingRule domainObjectRenamingRule = new DomainObjectRenamingRule();
                domainObjectRenamingRule.setSearchString(trimPattern);
                domainObjectRenamingRule.setReplaceString("");
                tableConfiguration.setColumnRenamingRule(columnRenamingRule);
                tableConfiguration.setDomainObjectRenamingRule(domainObjectRenamingRule);
            }
            return tableConfiguration;
        }).forEach(basicContext::addTableConfiguration);
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
        javaTypeResolverConfiguration.setConfigurationType("pro.chenggang.project.reactive.mybatis.support.generator.support.CustomJavaTypeResolver");
        javaTypeResolverConfiguration.addProperty("useJSR310Types","true");
        javaTypeResolverConfiguration.addProperty("forceBigDecimals","true");
        basicContext.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);
        return basicContext;
    }

}

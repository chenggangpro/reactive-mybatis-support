package pro.chenggang.project.reactive.mybatis.support.generator.core.context.impl;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin;
import pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.generator.DynamicGeneratorPlugin;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.other.RenameJavaMapperPlugin;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorProperties;

import java.util.Set;

import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.MODEL;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.MODEL_XML;
import static pro.chenggang.project.reactive.mybatis.support.generator.option.GeneratorType.SIMPLE;

/**
 * The Mybatis dynamic context generator.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class MybatisDynamicContextGenerator extends AbstractCommonContextGenerator {

    @Override
    public GeneratorType targetGeneratorType() {
        return GeneratorType.DYNAMIC;
    }

    @Override
    protected Context newContext() {
        Context context = new Context(ModelType.FLAT);
        context.setTargetRuntime("MyBatis3DynamicSQL");
        context.setId("MyBatis3Dynamic");
        context.addProperty("javaFileEncoding", "UTF-8");
        context.addProperty("columnOverride", "false");
        return context;
    }

    @Override
    protected void configureJavaClientGenerator(Context context, GeneratorProperties generatorProperties) {
        GeneratorProperties.TargetLocation targetLocation = generatorProperties.getTargetLocation();
        GeneratorProperties.TargetPackage targetPackage = generatorProperties.getTargetPackage();
        JavaClientGeneratorConfiguration clientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        clientGeneratorConfiguration.setTargetPackage(targetPackage.getFullMapperInterfacePackage() + ".dynamic");
        clientGeneratorConfiguration.setTargetProject(targetLocation.getFullJavaLocation());
        clientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(clientGeneratorConfiguration);
    }

    @Override
    protected void configureGeneratorPlugin(Context context, GeneratorProperties generatorProperties) {
        PluginConfiguration dynamicPluginConfiguration = new PluginConfiguration();
        dynamicPluginConfiguration.setConfigurationType(DynamicGeneratorPlugin.class.getCanonicalName());
        Set<GeneratorType> generatorTypes = generatorProperties.getGeneratorTypes();
        if (generatorTypes.contains(SIMPLE) || generatorTypes.contains(MODEL) || generatorTypes.contains(MODEL_XML)) {
            dynamicPluginConfiguration.addProperty("autoGenerateModel", "false");
        }
        context.addPluginConfiguration(dynamicPluginConfiguration);
        PluginConfiguration unMergeablePlugin = new PluginConfiguration();
        unMergeablePlugin.setConfigurationType(UnmergeableXmlMappersPlugin.class.getCanonicalName());
        context.addPluginConfiguration(unMergeablePlugin);
        PluginConfiguration renamePluginConfiguration = new PluginConfiguration();
        renamePluginConfiguration.setConfigurationType(RenameJavaMapperPlugin.class.getCanonicalName());
        renamePluginConfiguration.addProperty("searchString", "Mapper$");
        renamePluginConfiguration.addProperty("replaceString", "DynamicMapper");
        context.addPluginConfiguration(renamePluginConfiguration);
    }
}

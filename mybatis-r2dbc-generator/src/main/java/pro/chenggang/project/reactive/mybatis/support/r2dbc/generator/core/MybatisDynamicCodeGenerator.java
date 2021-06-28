package pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.core;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.internal.DefaultShellCallback;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.core.context.MyBatisSimpleContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.core.context.MybatisDynamicContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.properties.GeneratorExtensionProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: chenggang
 * @date 2020-01-21.
 */
public class MybatisDynamicCodeGenerator {

    private final GeneratorConfigurationFactory configurationFactory;

    private MybatisDynamicCodeGenerator(){
        this.configurationFactory = initConfigurationFactory();
    }

    private GeneratorConfigurationFactory initConfigurationFactory(){
        GeneratorConfigurationFactory configurationFactory = new GeneratorConfigurationFactory();
        configurationFactory.addContextGenerator(new MybatisDynamicContextGenerator());
        configurationFactory.addContextGenerator(new MyBatisSimpleContextGenerator());
        return configurationFactory;
    }

    public void generate(){
        initProperties(null,false);
        generateInternal();
    }

    public void generate(Class executeClass){
        if(Objects.isNull(executeClass)){
            throw new IllegalArgumentException("Execute Class Must Be Not Null");
        }
        initProperties(executeClass.getPackage().getName(),true);
        generateInternal();
    }

    public void generate(String basePackage){
        initProperties(basePackage,true);
        generateInternal();
    }

    private void initProperties(String basePackage, boolean forceBasePackage){
        GeneratorExtensionProperties properties = PropertiesHolder.getInstance().getProperties();
        if(forceBasePackage){
            properties.setBasePackage(basePackage);
        }
        properties.validateByDefault();
    }

    private void generateInternal(){
        Configuration configuration = this.configurationFactory.getConfiguration();
        GeneratorExtensionProperties properties = PropertiesHolder.getInstance().getProperties();
        DefaultShellCallback callback = new DefaultShellCallback(properties.isOverwrite());
        List<String> warnings = new ArrayList<>();
        try{
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, callback, warnings);
            myBatisGenerator.generate(new VerboseProgressCallback());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        warnings.forEach(System.out::println);
    }

    public static MybatisDynamicCodeGenerator getInstance(){
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder{

        private static final MybatisDynamicCodeGenerator INSTANCE;

        static {
            INSTANCE = new MybatisDynamicCodeGenerator();
        }
    }

}

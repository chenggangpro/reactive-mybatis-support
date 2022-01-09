package pro.chenggang.project.reactive.mybatis.support.generator.core;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.internal.DefaultShellCallback;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.MyBatisSimpleContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.core.context.MybatisDynamicContextGenerator;
import pro.chenggang.project.reactive.mybatis.support.generator.properties.GeneratorExtensionProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The type Mybatis dynamic code generator.
 *
 * @author Gang Cheng
 * @version 1.0.0
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

    /**
     * Generate.
     */
    public void generate(){
        initProperties(null,false);
        generateInternal();
    }

    /**
     * Generate.
     *
     * @param executeClass the execute class
     */
    public void generate(Class executeClass){
        if(Objects.isNull(executeClass)){
            throw new IllegalArgumentException("Execute Class Must Be Not Null");
        }
        initProperties(executeClass.getPackage().getName(),true);
        generateInternal();
    }

    /**
     * Generate.
     *
     * @param basePackage the base package
     */
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

    /**
     * Get instance mybatis dynamic code generator.
     *
     * @return the mybatis dynamic code generator
     */
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

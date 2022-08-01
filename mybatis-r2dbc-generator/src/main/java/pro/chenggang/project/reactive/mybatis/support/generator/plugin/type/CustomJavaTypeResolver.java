package pro.chenggang.project.reactive.mybatis.support.generator.plugin.type;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The custom java type resolver.
 */
public class CustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    private final ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    private final Map<String, GeneratedJavaTypeModifier> generatedJavaTypeModifierContainer = new ConcurrentHashMap<>();
    private String generatedJavaTypeModifierType;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.generatedJavaTypeModifierType = properties.getProperty("generatedJavaTypeModifierType","");
    }

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType defaultOverrideType = super.overrideDefaultType(column, defaultType);
        return this.getGeneratedJavaTypeModifier()
                .map(modifier -> modifier.overrideDefaultType(column, defaultOverrideType))
                .orElse(defaultOverrideType);

    }

    /**
     * get generated java type modifier
     *
     * @return the optional GeneratedJavaTypeModifier
     */
    private Optional<GeneratedJavaTypeModifier> getGeneratedJavaTypeModifier() {
        return Optional.ofNullable(this.generatedJavaTypeModifierType)
                .filter(StringUtils::isNotBlank)
                .map(modifierType -> generatedJavaTypeModifierContainer
                        .computeIfAbsent(modifierType, type -> {
                            Class<?> aClass = null;
                            try {
                                aClass = Class.forName(type);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException("Can not find GeneratedJavaTypeModifier Class ," +
                                        "ClassName: " + type, e);
                            }
                            Reflector reflector = reflectorFactory.findForClass(aClass);
                            try {
                                return (GeneratedJavaTypeModifier) reflector.getDefaultConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Can not initialize GeneratedJavaTypeModifier " +
                                        "with default constructor ,ClassName:" + type, e);
                            }
                        })
                );
    }

}

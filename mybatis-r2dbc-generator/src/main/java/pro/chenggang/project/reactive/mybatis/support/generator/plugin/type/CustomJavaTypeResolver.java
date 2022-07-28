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
import java.util.concurrent.ConcurrentHashMap;

/**
 * The custom java type resolver.
 */
public class CustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    private final ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    private final Map<String, GeneratedJavaTypeModifier> defaultJavaTypeModifierContainer = new ConcurrentHashMap<>();
    private String defaultJavaTypeModifierType;

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType defaultOverrideType = super.overrideDefaultType(column, defaultType);
        return this.getDefaultJavaTypeModifier()
                .map(modifier -> modifier.overrideDefaultType(column, defaultOverrideType))
                .orElse(defaultOverrideType);

    }

    /**
     * get default java type modifier
     *
     * @return the optional DefaultJavaTypeModifier
     */
    private Optional<GeneratedJavaTypeModifier> getDefaultJavaTypeModifier() {
        return Optional.ofNullable(this.defaultJavaTypeModifierType)
                .filter(StringUtils::isNotBlank)
                .map(modifierType -> defaultJavaTypeModifierContainer
                        .computeIfAbsent(modifierType, type -> {
                            Class<?> aClass = null;
                            try {
                                aClass = Class.forName(type);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException("Can not find DefaultJavaTypeModifier Class ," +
                                        "ClassName: " + type, e);
                            }
                            Reflector reflector = reflectorFactory.findForClass(aClass);
                            try {
                                return (GeneratedJavaTypeModifier) reflector.getDefaultConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Can not initialize DefaultJavaTypeModifier " +
                                        "with default constructor ,ClassName:" + type, e);
                            }
                        })
                );
    }

}

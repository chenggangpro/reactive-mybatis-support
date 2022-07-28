package pro.chenggang.project.reactive.mybatis.support.generator.plugin.type;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface GeneratedJavaTypeModifier {

    /**
     * Override default type
     *
     * @param column      the introspected column
     * @param defaultType the default processed java type
     * @return the override java type
     */
    FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType);
}

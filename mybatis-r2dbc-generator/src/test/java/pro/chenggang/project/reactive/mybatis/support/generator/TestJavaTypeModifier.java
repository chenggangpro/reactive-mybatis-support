package pro.chenggang.project.reactive.mybatis.support.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import pro.chenggang.project.reactive.mybatis.support.generator.plugin.type.GeneratedJavaTypeModifier;

import java.sql.Types;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class TestJavaTypeModifier implements GeneratedJavaTypeModifier {

    @Override
    public FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        if (Types.TINYINT == column.getJdbcType()) {
            return new FullyQualifiedJavaType(Integer.class.getName());
        }
        return defaultType;
    }
}

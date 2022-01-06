package pro.chenggang.project.reactive.mybatis.support.generator.support;

import lombok.NoArgsConstructor;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * The type Trim table name plugin.
 * @author chenggang
 * @version 1.0.0
 */
@NoArgsConstructor
public class TrimTableNamePlugin extends PluginAdapter {

    private String prefix;

	@Override
    public boolean validate(List<String> warnings) {
        String trimPrefix = properties.getProperty("tablePrefix");
        boolean valid = stringHasValue(trimPrefix);
        if (valid) {
            prefix = upperFirstLetter(trimPrefix);
        } else {
            if (!stringHasValue(trimPrefix)) {
                warnings.add(getString("ValidationError.18",
                        "TrimTableNamePlugin",
                        "trimPrefix"));
            }
        }
        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        introspectedTable.setMyBatis3JavaMapperType(trimPrefix(introspectedTable.getMyBatis3JavaMapperType()));
        introspectedTable.setMyBatisDynamicSqlSupportType(trimPrefix(introspectedTable.getMyBatisDynamicSqlSupportType()));
        introspectedTable.setMyBatis3XmlMapperFileName(trimPrefix(introspectedTable.getMyBatis3XmlMapperFileName()));


    }

    private String trimPrefix(String name){
        String[] split = name.split("\\.");
        if(split.length == 0){
            return name;
        }
        String last = split[split.length - 1];
        if(!StringUtility.stringHasValue(last)){
            return name;
        }
        if(!last.startsWith(this.prefix)){
            return name;
        }
        last = last.replaceFirst(this.prefix,"");
        split[split.length-1] = last;
        return String.join(".",split);
    }

    private String upperFirstLetter(String letter){
        char[] chars = letter.toCharArray();
        if(chars[0]>='a' && chars[0]<='z'){
            chars[0] = (char) (chars[0]-32);
        }
        return new String(chars);
    }
}
package pro.chenggang.project.reactive.mybatis.support.r2dbc.generator.support;

import lombok.NoArgsConstructor;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

@NoArgsConstructor
public class RenameJavaMapperPlugin extends PluginAdapter {
    private String replaceString;
    private Pattern pattern;

	@Override
    public boolean validate(List<String> warnings) {
        String searchString = properties.getProperty("searchString");
        this.replaceString = properties.getProperty("replaceString");
        boolean valid = stringHasValue(searchString) && stringHasValue(replaceString);
        if (valid) {
            pattern = Pattern.compile(searchString);
        } else {
            if (!stringHasValue(searchString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameJavaMapperPlugin",
                        "searchString"));
            }
            if (!stringHasValue(replaceString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameJavaMapperPlugin",
                        "replaceString"));
            }
        }
        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getMyBatis3JavaMapperType();
        Matcher matcher = pattern.matcher(oldType);
        oldType = matcher.replaceAll(replaceString);
        introspectedTable.setMyBatis3JavaMapperType(oldType);
    }
}
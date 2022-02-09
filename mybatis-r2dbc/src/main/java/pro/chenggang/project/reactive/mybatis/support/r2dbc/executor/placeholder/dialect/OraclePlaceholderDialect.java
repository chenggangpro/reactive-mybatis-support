package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

/**
 * Oracle placeholder dialect
 * @author Gang Cheng
 * @since 1.0.5
 * @version 1.0.5
 */
public class OraclePlaceholderDialect implements NamePlaceholderDialect {

    @Override
    public String name() {
        return "Oracle";
    }

    @Override
    public String getMarker() {
        return ":";
    }

}

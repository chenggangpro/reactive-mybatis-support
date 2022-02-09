package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

/**
 * H2 placeholder dialect
 * @author Gang Cheng
 * @since 1.0.5
 * @version 1.0.5
 */
public class H2PlaceholderDialect extends PostgreSQLPlaceholderDialect {

    @Override
    public String name() {
        return "H2";
    }

}

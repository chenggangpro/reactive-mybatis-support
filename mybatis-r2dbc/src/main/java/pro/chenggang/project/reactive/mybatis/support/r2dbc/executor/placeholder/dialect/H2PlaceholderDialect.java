package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

/**
 * H2 placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class H2PlaceholderDialect extends PostgreSQLPlaceholderDialect {

    /**
     * The dialect name
     */
    public static final String DIALECT_NAME = "H2";

    @Override
    public String name() {
        return DIALECT_NAME;
    }

}

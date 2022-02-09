package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

/**
 * Microsoft SQL Server placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class SQLServerPlaceholderDialect implements NamePlaceholderDialect {

    /**
     * The dialect name
     */
    public static final String DIALECT_NAME = "Microsoft SQL Server";

    @Override
    public String name() {
        return DIALECT_NAME;
    }

    @Override
    public String getMarker() {
        return "@";
    }

}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

/**
 * Microsoft SQL Server placeholder dialect
 * @author Gang Cheng
 * @since 1.0.5
 * @version 1.0.5
 */
public class SQLServerPlaceholderDialect implements NamePlaceholderDialect {

    @Override
    public String name() {
        return "Microsoft SQL Server";
    }

    @Override
    public String getMarker() {
        return "@";
    }

}

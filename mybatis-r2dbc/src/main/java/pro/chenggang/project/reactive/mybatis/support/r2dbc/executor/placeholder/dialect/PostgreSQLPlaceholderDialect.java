package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;

/**
 * PostgreSQL placeholder dialect
 * @author Gang Cheng
 * @since 1.0.5
 * @version 1.0.5
 */
public class PostgreSQLPlaceholderDialect implements PlaceholderDialect {

    @Override
    public String name() {
        return "PostgreSQL";
    }

    @Override
    public String getMarker() {
        return "$";
    }

    @Override
    public int startIndex() {
        return 1;
    }
}

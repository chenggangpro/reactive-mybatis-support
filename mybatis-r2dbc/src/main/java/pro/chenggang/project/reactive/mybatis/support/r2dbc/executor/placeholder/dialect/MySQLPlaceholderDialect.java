package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;

/**
 * MySQL placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class MySQLPlaceholderDialect implements PlaceholderDialect {

    /**
     * The dialect name.
     */
    public static final String DIALECT_NAME = "MySQL";

    @Override
    public String name() {
        return DIALECT_NAME;
    }

}

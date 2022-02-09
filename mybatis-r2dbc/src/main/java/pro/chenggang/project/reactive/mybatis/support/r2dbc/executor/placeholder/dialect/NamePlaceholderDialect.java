package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;

/**
 * Name placeholder dialect
 * @author Gang Cheng
 * @since 1.0.5
 * @version 1.0.5
 */
public interface NamePlaceholderDialect extends PlaceholderDialect {

    @Override
    default boolean usingIndexMarker() {
        return false;
    }
}

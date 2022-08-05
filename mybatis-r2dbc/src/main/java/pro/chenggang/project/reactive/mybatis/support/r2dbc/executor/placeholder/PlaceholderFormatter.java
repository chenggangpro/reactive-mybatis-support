package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionMetadata;
import org.apache.ibatis.mapping.BoundSql;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

/**
 * The Placeholder formatter.
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderFormatter {

    /**
     * Replace sql placeholder string.
     *
     * @param connectionMetadata                the connection metadata
     * @param boundSql                         the bound sql
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the string
     */
    String replaceSqlPlaceholder(ConnectionMetadata connectionMetadata, BoundSql boundSql, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute);
}

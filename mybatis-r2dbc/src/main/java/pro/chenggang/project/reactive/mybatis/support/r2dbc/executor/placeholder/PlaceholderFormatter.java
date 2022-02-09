package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;
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
     * @param connectionFactory                the connection factory
     * @param mappedStatementId                the mapped statement id
     * @param boundSql                         the bound sql
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the string
     */
    String replaceSqlPlaceholder(ConnectionFactory connectionFactory, String mappedStatementId, BoundSql boundSql, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute);
}

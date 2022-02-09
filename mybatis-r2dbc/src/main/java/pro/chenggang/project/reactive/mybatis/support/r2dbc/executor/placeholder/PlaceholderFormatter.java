package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.mapping.BoundSql;

/**
 * The interface Placeholder formatter.
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderFormatter {

    /**
     * Format placeholder string.
     *
     * @param connectionFactory the connection factory
     * @param boundSql          the bound sql
     * @return the string
     */
    String formatPlaceholder(ConnectionFactory connectionFactory, BoundSql boundSql);
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Optional;

/**
 * The Placeholder dialect registry.
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderDialectRegistry {

    /**
     * Register PlaceholderDialect
     *
     * @param placeholderDialect the placeholder dialect
     */
    void register(PlaceholderDialect placeholderDialect);

    /**
     * Gets placeholder dialect.
     *
     * @param connectionFactory the connection factory
     * @return the placeholder dialect
     */
    Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionFactory connectionFactory);
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.util.Optional;
import java.util.Set;

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
     * Get all PlaceholderDialect type
     * @return all PlaceholderDialect's type set
     */
    Set<Class<? extends PlaceholderDialect>> getAllPlaceholderDialectTypes();

    /**
     * Gets placeholder dialect.
     *
     * @param connectionFactory                the connection factory
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the placeholder dialect
     */
    Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionFactory connectionFactory, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute);
}

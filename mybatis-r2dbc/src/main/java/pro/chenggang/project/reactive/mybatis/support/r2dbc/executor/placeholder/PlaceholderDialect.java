package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.util.Locale;
import java.util.Optional;

/**
 * Placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderDialect {

    /**
     * The constant PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY.
     */
    String PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY = "PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY";

    /**
     * The constant DEFAULT_PLACEHOLDER
     */
    String DEFAULT_PLACEHOLDER = "?";

    /**
     * Dialect name.
     *
     * @return the dialect name
     */
    String name();

    /**
     * Supported boolean.
     *
     * @param connectionFactory                the connection factory
     * @param reactiveExecutorContextAttribute the reactive executor context attribute
     * @return the boolean
     */
    default boolean supported(ConnectionFactory connectionFactory, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        String name = Optional.ofNullable(reactiveExecutorContextAttribute
                        .getAttribute()
                        .get(PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY)
                )
                .filter(value -> value instanceof String)
                .map(String.class::cast)
                .orElseGet(() -> connectionFactory.getMetadata().getName());
        return name.equalsIgnoreCase(this.name())
                || name.toLowerCase(Locale.ENGLISH).contains(this.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get marker string.
     *
     * @return the marker
     */
    default String getMarker() {
        return DEFAULT_PLACEHOLDER;
    }

    /**
     * Using index marker.
     *
     * @return the boolean
     */
    default boolean usingIndexMarker() {
        return true;
    }

    /**
     * Placeholder start index.
     *
     * @return the int default is 0
     */
    default int startIndex() {
        return 0;
    }
}

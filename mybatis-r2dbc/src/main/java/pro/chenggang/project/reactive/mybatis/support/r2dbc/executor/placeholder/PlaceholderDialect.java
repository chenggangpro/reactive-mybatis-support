package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Locale;

/**
 * Placeholder dialect
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public interface PlaceholderDialect {

    /**
     * Dialect name.
     *
     * @return the dialect name
     */
    String name();

    /**
     * Supported boolean.
     *
     * @param connectionFactory the connection factory
     * @return the boolean
     */
    default boolean supported(ConnectionFactory connectionFactory) {
        String name = connectionFactory.getMetadata().getName();
        return name.equalsIgnoreCase(this.name())
                || name.toLowerCase(Locale.ENGLISH).contains(this.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get marker string.
     *
     * @return the marker
     */
    String getMarker();

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

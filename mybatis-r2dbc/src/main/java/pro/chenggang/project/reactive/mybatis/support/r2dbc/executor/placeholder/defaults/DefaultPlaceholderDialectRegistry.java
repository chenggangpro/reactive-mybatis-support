package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults;

import io.r2dbc.spi.ConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.H2PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.MariaDBPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.MySQLPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.OraclePlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.PostgreSQLPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.SQLServerPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect.PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY;

/**
 * Default placeholder dialect factory
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class DefaultPlaceholderDialectRegistry implements PlaceholderDialectRegistry {

    private final Map<String, PlaceholderDialect> placeholderDialects = new HashMap<>();

    public DefaultPlaceholderDialectRegistry() {
        this.register(new MySQLPlaceholderDialect());
        this.register(new MariaDBPlaceholderDialect());
        this.register(new PostgreSQLPlaceholderDialect());
        this.register(new H2PlaceholderDialect());
        this.register(new OraclePlaceholderDialect());
        this.register(new SQLServerPlaceholderDialect());
    }

    @Override
    public void register(PlaceholderDialect placeholderDialect) {
        this.placeholderDialects.put(placeholderDialect.name().toLowerCase(Locale.ENGLISH), placeholderDialect);
    }

    @Override
    public Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionFactory connectionFactory, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        String name = Optional.ofNullable(reactiveExecutorContextAttribute.getAttribute().get(PLACEHOLDER_DIALECT_NAME_ATTRIBUTE_KEY))
                .filter(value -> value instanceof String)
                .map(String.class::cast)
                .orElseGet(() -> connectionFactory.getMetadata().getName());
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        if (this.placeholderDialects.containsKey(lowerCaseName)) {
            return Optional.of(this.placeholderDialects.get(lowerCaseName));
        }
        return this.placeholderDialects
                .values()
                .stream()
                .filter(placeholderDialect -> placeholderDialect.supported(connectionFactory,reactiveExecutorContextAttribute))
                .findFirst();
    }
}

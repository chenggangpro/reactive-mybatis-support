package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults;

import io.r2dbc.spi.ConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.H2PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.SQLServerPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.OraclePlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.PostgreSQLPlaceholderDialect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Default placeholder dialect factory
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class DefaultPlaceholderDialectRegistry implements PlaceholderDialectRegistry {

    private final Map<String, PlaceholderDialect> placeholderDialects = new HashMap<>();
    private final Set<String> ignorePlaceholderDialectNames = new HashSet<>();

    public DefaultPlaceholderDialectRegistry() {
        this.register(new PostgreSQLPlaceholderDialect());
        this.register(new H2PlaceholderDialect());
        this.register(new OraclePlaceholderDialect());
        this.register(new SQLServerPlaceholderDialect());
        this.initIgnorePlaceholderDialect();
    }

    /**
     * init ignore placeholder dialect
     */
    private void initIgnorePlaceholderDialect() {
        this.placeholderDialects.keySet()
                .forEach(key -> {
                    boolean isMySQL = key.equalsIgnoreCase("MySQL") || key.toLowerCase(Locale.ENGLISH).contains("mysql");
                    if (!isMySQL) {
                        this.ignorePlaceholderDialectNames.add("mysql");
                        return;
                    }
                    boolean isMariaDB = key.equalsIgnoreCase("MariaDB") || key.toLowerCase(Locale.ENGLISH).contains("mariadb");
                    if (!isMariaDB) {
                        this.ignorePlaceholderDialectNames.add("mariadb");
                    }
                });
    }

    @Override
    public void register(PlaceholderDialect placeholderDialect) {
        this.placeholderDialects.put(placeholderDialect.name().toLowerCase(Locale.ENGLISH), placeholderDialect);
    }

    @Override
    public Optional<PlaceholderDialect> getPlaceholderDialect(ConnectionFactory connectionFactory) {
        String name = connectionFactory.getMetadata().getName();
        boolean anyMatchIgnoreDialect = ignorePlaceholderDialectNames.stream()
                .anyMatch(ignoreName -> name.equalsIgnoreCase(ignoreName)
                        || name.toLowerCase(Locale.ENGLISH).contains(ignoreName)
                );
        if (anyMatchIgnoreDialect) {
            return Optional.empty();
        }
        if (this.placeholderDialects.containsKey(name)) {
            return Optional.of(this.placeholderDialects.get(name));
        }
        return this.placeholderDialects
                .values()
                .stream()
                .filter(placeholderDialect -> placeholderDialect.supported(connectionFactory))
                .findFirst();
    }
}

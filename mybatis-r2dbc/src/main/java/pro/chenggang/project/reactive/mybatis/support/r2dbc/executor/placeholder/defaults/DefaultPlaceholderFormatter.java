package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.r2dbc.spi.ConnectionMetadata;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.util.MapUtil;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialectRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderFormatter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.PlaceholderDialect.DEFAULT_PLACEHOLDER;

/**
 * Default placeholder formatter
 *
 * @author Gang Cheng
 * @version 1.0.5
 * @since 1.0.5
 */
public class DefaultPlaceholderFormatter implements PlaceholderFormatter {

    private static final Log log = LogFactory.getLog(DefaultPlaceholderFormatter.class);

    private final PlaceholderDialectRegistry placeholderDialectRegistry;
    //Class<? extends PlaceholderDialect --> Cache< original SQL , formatted SQL >
    private final ConcurrentHashMap<Class<? extends PlaceholderDialect>, Cache<String, String>> formattedSqlCache = new ConcurrentHashMap<>();

    public DefaultPlaceholderFormatter(PlaceholderDialectRegistry placeholderDialectRegistry, Integer sqlCacheMaxSize, Duration sqlCacheExpireDuration) {
        this.placeholderDialectRegistry = placeholderDialectRegistry;
        Set<Class<? extends PlaceholderDialect>> allPlaceholderDialectTypes = placeholderDialectRegistry.getAllPlaceholderDialectTypes();
        for (Class<? extends PlaceholderDialect> placeholderDialectType : allPlaceholderDialectTypes) {
            Cache<String, String> cache = Caffeine.newBuilder()
                    .maximumSize(sqlCacheMaxSize)
                    .expireAfterAccess(sqlCacheExpireDuration)
                    .initialCapacity(10)
                    .build();
            this.formattedSqlCache.put(placeholderDialectType, cache);
        }
    }

    @Override
    public String replaceSqlPlaceholder(ConnectionMetadata connectionMetadata, BoundSql boundSql, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        Optional<PlaceholderDialect> optionalPlaceholderDialect = placeholderDialectRegistry
                .getPlaceholderDialect(connectionMetadata, reactiveExecutorContextAttribute)
                .filter(placeholderDialect -> !Objects.equals(placeholderDialect.getMarker(), DEFAULT_PLACEHOLDER));
        String originalSql = boundSql.getSql();
        if (!optionalPlaceholderDialect.isPresent()) {
            if (log.isTraceEnabled()) {
                log.trace("Placeholder dialect not found or is default placeholder ,use original sql");
            }
            return originalSql;
        }
        PlaceholderDialect placeholderDialect = optionalPlaceholderDialect.get();
        Cache<String, String> cache = this.formattedSqlCache.get(placeholderDialect.getClass());
        if (Objects.isNull(cache)) {
            throw new IllegalStateException("Placeholder dialect found,but Placeholder dialect sql cache is null,Placeholder dialect type : " + placeholderDialect.getClass());
        }
        return MapUtil.computeIfAbsent(cache.asMap(),
                originalSql,
                statementId -> this.formatPlaceholderInternal(placeholderDialect, boundSql)
        );
    }

    /**
     * format placeholder internal
     *
     * @param placeholderDialect the placeholder dialect
     * @param boundSql           the original boundSql sql
     * @return formatted sql
     */
    protected String formatPlaceholderInternal(PlaceholderDialect placeholderDialect, BoundSql boundSql) {
        String sql = boundSql.getSql();
        char defaultPlaceholder = DEFAULT_PLACEHOLDER.charAt(0);
        if (sql.indexOf(defaultPlaceholder) < 0) {
            if (log.isTraceEnabled()) {
                log.trace("Placeholder not found ,use original sql");
            }
            return sql;
        }
        int length = sql.length();
        String marker = placeholderDialect.getMarker();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        int identifierIndex = placeholderDialect.usingIndexMarker() ? placeholderDialect.startIndex() : 0;
        StringBuilder builder = new StringBuilder(length + 10);
        int begin = 0;
        for (int i = 0; i < length; i++) {
            char aChar = sql.charAt(i);
            if (aChar != defaultPlaceholder) {
                continue;
            }
            boolean previousMatched = i != 0 && sql.charAt(i - 1) == defaultPlaceholder;
            boolean forwardMatched = i < length - 1 && sql.charAt(i + 1) == defaultPlaceholder;
            if (previousMatched || forwardMatched) {
                continue;
            }
            if (placeholderDialect.usingIndexMarker()) {
                builder.append(sql, begin, i).append(marker).append(identifierIndex);
            } else {
                String parameterProperty = parameterMappings.get(identifierIndex).getProperty()
                        .replaceAll("\\.", "_");
                builder.append(sql, begin, i).append(marker).append(parameterProperty);
            }
            identifierIndex++;
            begin = i + 1;
        }
        if (begin != length) {
            builder.append(sql, begin, length);
        }
        if (log.isDebugEnabled()) {
            if (placeholderDialect.usingIndexMarker()) {
                log.debug("Format placeholder based by index, with (" + placeholderDialect.getClass().getSimpleName() + ")");
            } else {
                log.debug("Format placeholder based on parameter name, with (" + placeholderDialect.getClass().getSimpleName() + ")");
            }
            log.debug("Formatted SQL  => " + builder);
        }
        return builder.toString();
    }

}

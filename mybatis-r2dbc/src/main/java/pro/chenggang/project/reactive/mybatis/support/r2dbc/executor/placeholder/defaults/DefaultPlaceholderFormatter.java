package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.defaults;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.r2dbc.spi.ConnectionFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final Pattern jdbcPlaceholderPattern = Pattern.compile("\\?");
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
    public String replaceSqlPlaceholder(ConnectionFactory connectionFactory, BoundSql boundSql, ReactiveExecutorContextAttribute reactiveExecutorContextAttribute) {
        Optional<PlaceholderDialect> optionalPlaceholderDialect = placeholderDialectRegistry
                .getPlaceholderDialect(connectionFactory, reactiveExecutorContextAttribute)
                .filter(placeholderDialect -> !Objects.equals(placeholderDialect.getMarker(), DEFAULT_PLACEHOLDER));
        String originalSql = boundSql.getSql();
        if (!optionalPlaceholderDialect.isPresent()) {
            if (log.isTraceEnabled()) {
                log.trace("Placeholder dialect not found ,use original sql");
            }
            return originalSql;
        }
        PlaceholderDialect placeholderDialect = optionalPlaceholderDialect.get();
        Cache<String, String> cache = this.formattedSqlCache.get(placeholderDialect.getClass());
        if (Objects.isNull(cache)) {
            throw new IllegalStateException("Placeholder dialect is found,but Placeholder dialect sql cache is null,Placeholder dialect type : " + placeholderDialect.getClass());
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
        List<Integer> placeholderIndexList = this.extractJdbcPlaceholderIndex(sql);
        if (placeholderIndexList.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("Placeholder index not found ,use original sql");
            }
            return sql;
        }
        String marker = placeholderDialect.getMarker();
        int startIndex = placeholderDialect.startIndex();
        StringBuilder builder = new StringBuilder();
        if (placeholderDialect.usingIndexMarker()) {
            for (int i = 0; i < placeholderIndexList.size(); i++) {
                Integer placeholderIndexValue = placeholderIndexList.get(i);
                builder.append(sql, i == 0 ? 0 : placeholderIndexList.get(i - 1) + 1, placeholderIndexValue)
                        .append(marker)
                        .append(i + startIndex);
            }
            if (placeholderIndexList.get(placeholderIndexList.size() - 1) < sql.length()) {
                builder.append(sql, placeholderIndexList.get(placeholderIndexList.size() - 1) + 1, sql.length());
            }
            String formattedSql = builder.toString();
            if (log.isDebugEnabled()) {
                log.debug("Format placeholder based by index, with (" + placeholderDialect.getClass().getSimpleName() + ") => " + formattedSql);
            }
            return formattedSql;
        }
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < placeholderIndexList.size(); i++) {
            Integer placeholderIndexValue = placeholderIndexList.get(i);
            builder.append(sql, i == 0 ? 0 : placeholderIndexList.get(i - 1) + 1, placeholderIndexValue)
                    .append(marker);
            String parameterProperty = parameterMappings.get(i).getProperty().replaceAll("\\.", "_");
            builder.append(parameterProperty);
        }
        if (placeholderIndexList.get(placeholderIndexList.size() - 1) < sql.length()) {
            builder.append(sql, placeholderIndexList.get(placeholderIndexList.size() - 1) + 1, sql.length());
        }
        String formattedSql = builder.toString();
        if (log.isDebugEnabled()) {
            log.debug("Format placeholder based on parameter name, with (" + placeholderDialect.getClass().getSimpleName() + ") \n => " + formattedSql);
        }
        return formattedSql;
    }

    /**
     * extract jdbc placeholder index
     *
     * @param sql the original sql
     * @return the index list
     */
    protected List<Integer> extractJdbcPlaceholderIndex(String sql) {
        Matcher matcher = jdbcPlaceholderPattern.matcher(sql);
        List<Integer> indexList = new ArrayList<>();
        int previous = -1;
        int result = -1;
        while (matcher.find()) {
            int start = matcher.start();
            if (previous < 0) {
                previous = start;
                result = start;
                continue;
            }
            if (start - previous == 1) {
                previous = start;
                if (result >= 0) {
                    result = -1;
                }
                continue;
            }
            if (result >= 0) {
                indexList.add(result);
            }
            previous = start;
            result = start;
        }
        if (result > 0) {
            indexList.add(result);
        }
        return indexList;
    }

}

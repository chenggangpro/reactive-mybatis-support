package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.ArrayDeque;
import java.util.function.Function;

/**
 * The r2dbc mybatis database routing context manager.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class R2dbcMybatisDatabaseRoutingContextManager {

    /**
     * Gets current database routing key info from current routing context
     *
     * @return the database routing key info
     */
    public static Mono<R2dbcMybatisDatabaseRoutingKeyInfo> currentRoutingContext() {
        return Mono.deferContextual(contextView -> Mono.justOrEmpty(contextView.getOrEmpty(
                R2dbcMybatisDatabaseRoutingKeyInfo.class)));
    }

    /**
     * Gets current database routing context holder from current routing context
     *
     * @return the database routing context holder
     */
    public static Mono<R2dbcMybatisDatabaseRoutingContextHolder> currentRoutingHolderContext() {
        return Mono.deferContextual(contextView -> Mono.justOrEmpty(contextView.getOrEmpty(
                R2dbcMybatisDatabaseRoutingContextHolder.class)));
    }

    /**
     * Initialize database routing context with target routing key.
     *
     * @param targetRoutingKey the target routing key
     * @return the context initialization function
     */
    public static Function<Context, Context> initializeDatabaseRoutingContext(String targetRoutingKey) {
        return context -> {
            R2dbcMybatisDatabaseRoutingKeyInfo r2dbcMybatisDatabaseRoutingKeyInfo = R2dbcMybatisDatabaseRoutingKeyInfo.of(targetRoutingKey);
            if (context.hasKey(R2dbcMybatisDatabaseRoutingContextHolder.class)) {
                R2dbcMybatisDatabaseRoutingContextHolder r2dbcMybatisDatabaseRoutingContextHolder = context.get(
                        R2dbcMybatisDatabaseRoutingContextHolder.class);
                r2dbcMybatisDatabaseRoutingContextHolder.getDatabaseRoutingKeys().push(
                        r2dbcMybatisDatabaseRoutingKeyInfo);
                log.debug("Initialize database routing context with target routing key :{}", targetRoutingKey);
                return context.put(R2dbcMybatisDatabaseRoutingKeyInfo.class, r2dbcMybatisDatabaseRoutingKeyInfo);
            }
            R2dbcMybatisDatabaseRoutingContextHolder r2dbcMybatisDatabaseRoutingContextHolder = R2dbcMybatisDatabaseRoutingContextHolder.of(
                    new ArrayDeque<>());
            r2dbcMybatisDatabaseRoutingContextHolder.getDatabaseRoutingKeys().push(r2dbcMybatisDatabaseRoutingKeyInfo);
            log.debug("Add database routing context with target routing key :{}", targetRoutingKey);
            return context.put(R2dbcMybatisDatabaseRoutingContextHolder.class, r2dbcMybatisDatabaseRoutingContextHolder)
                    .put(R2dbcMybatisDatabaseRoutingKeyInfo.class, r2dbcMybatisDatabaseRoutingKeyInfo);
        };
    }

    /**
     * Initialize database routing holder context.
     *
     * @return the context initialization function
     */
    public static Function<Context, Context> initializeDatabaseRoutingHolderContext() {
        return context -> {
            if (context.hasKey(R2dbcMybatisDatabaseRoutingContextHolder.class)) {
                return context;
            }
            log.debug("Initialize database routing context holder success");
            return context.put(R2dbcMybatisDatabaseRoutingContextHolder.class,
                    R2dbcMybatisDatabaseRoutingContextHolder.of(new ArrayDeque<>())
            );
        };
    }

}

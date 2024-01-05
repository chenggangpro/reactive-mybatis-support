package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * The r2dbc mybatis database routing key info.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Getter
@ToString
public class R2dbcMybatisDatabaseRoutingKeyInfo {

    private final String routingKey;

    private R2dbcMybatisDatabaseRoutingKeyInfo(String routingKey) {
        Objects.requireNonNull(routingKey, "The database routing key can not be null");
        this.routingKey = routingKey;
    }

    /**
     * New database routing key info.
     *
     * @param routingKey the routing key
     * @return the database routing key info
     */
    public static R2dbcMybatisDatabaseRoutingKeyInfo of(String routingKey) {
        return new R2dbcMybatisDatabaseRoutingKeyInfo(routingKey);
    }
}
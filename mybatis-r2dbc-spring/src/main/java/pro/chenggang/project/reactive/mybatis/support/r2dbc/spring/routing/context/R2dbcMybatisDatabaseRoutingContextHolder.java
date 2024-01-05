package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context;

import lombok.Value;

import java.util.ArrayDeque;

/**
 * The r2dbc mybatis database routing context holder.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Value(staticConstructor = "of")
public class R2dbcMybatisDatabaseRoutingContextHolder {

    ArrayDeque<R2dbcMybatisDatabaseRoutingKeyInfo> databaseRoutingKeys;

}

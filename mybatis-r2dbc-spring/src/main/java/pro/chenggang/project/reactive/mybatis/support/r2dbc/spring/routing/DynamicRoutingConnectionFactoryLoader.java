package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Map;

/**
 * The interface Dynamic routing connection factory loader.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@FunctionalInterface
public interface DynamicRoutingConnectionFactoryLoader {

    /**
     * Load dynamic routing connection factory.
     *
     * @return the dynamic routing connection factory map
     */
    Map<String, ConnectionFactory> load();
}

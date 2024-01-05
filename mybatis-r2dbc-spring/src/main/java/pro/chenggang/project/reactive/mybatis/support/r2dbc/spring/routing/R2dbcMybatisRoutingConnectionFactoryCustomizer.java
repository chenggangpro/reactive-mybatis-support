
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing;

/**
 * Dynamic routing connection factory customizer
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface R2dbcMybatisRoutingConnectionFactoryCustomizer {

    /**
     * customize dynamic routing connection factory
     *
     * @param r2dbcMybatisDynamicRoutingConnectionFactory the dynamic routing connection factory
     */
    void customize(R2dbcMybatisDynamicRoutingConnectionFactory r2dbcMybatisDynamicRoutingConnectionFactory);
}

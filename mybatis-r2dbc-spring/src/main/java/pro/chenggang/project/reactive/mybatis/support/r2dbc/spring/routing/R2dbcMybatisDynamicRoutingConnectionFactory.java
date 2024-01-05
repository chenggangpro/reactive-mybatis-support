
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.MySQLPlaceholderDialect;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context.R2dbcMybatisDatabaseRoutingContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context.R2dbcMybatisDatabaseRoutingKeyInfo;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * The r2dbc mybatis dynamic routing connection factory
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
@Slf4j
public class R2dbcMybatisDynamicRoutingConnectionFactory extends AbstractRoutingConnectionFactory implements ApplicationContextAware {

    private final DynamicRoutingConnectionFactoryLoader dynamicRoutingConnectionFactoryLoader;
    private ConnectionFactory defaultConnectionFactory;
    private ApplicationContext applicationContext;

    public R2dbcMybatisDynamicRoutingConnectionFactory(DynamicRoutingConnectionFactoryLoader dynamicRoutingConnectionFactoryLoader) {
        this(null, dynamicRoutingConnectionFactoryLoader);
    }

    public R2dbcMybatisDynamicRoutingConnectionFactory(ConnectionFactory defaultConnectionFactory,
                                                       DynamicRoutingConnectionFactoryLoader dynamicRoutingConnectionFactoryLoader) {
        this.defaultConnectionFactory = defaultConnectionFactory;
        this.dynamicRoutingConnectionFactoryLoader = dynamicRoutingConnectionFactoryLoader;
    }

    @Override
    public ConnectionFactoryMetadata getMetadata() {
        if (Objects.nonNull(defaultConnectionFactory)) {
            return super.getMetadata();
        }
        return () -> MySQLPlaceholderDialect.DIALECT_NAME;
    }

    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return R2dbcMybatisDatabaseRoutingContextManager.currentRoutingContext()
                .map(R2dbcMybatisDatabaseRoutingKeyInfo::getRoutingKey)
                .cast(Object.class)
                .doOnNext(value -> log.info("Determine current connection factory lookup key :" + value));
    }

    /**
     * Expose determineTargetConnectionFactory operation
     * from {@link AbstractRoutingConnectionFactory#determineTargetConnectionFactory()}
     *
     * @return emitting the current connection factory
     */
    public Mono<ConnectionFactory> determineTargetConnectionFactory() {
        return super.determineTargetConnectionFactory();
    }

    @Override
    public void afterPropertiesSet() {
        if (Objects.isNull(this.defaultConnectionFactory)) {
            this.defaultConnectionFactory = this.applicationContext.getBean(StringUtils.uncapitalize(ConnectionFactory.class.getSimpleName()),
                    ConnectionFactory.class
            );
            this.setDefaultTargetConnectionFactory(this.defaultConnectionFactory);
        }
        this.setTargetConnectionFactories(this.loadConnectionFactory());
        applicationContext.getBeansOfType(R2dbcMybatisRoutingConnectionFactoryCustomizer.class)
                .forEach((k, v) -> v.customize(this));
        super.afterPropertiesSet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Load all connection factories
     *
     * @return all target connection factories
     */
    protected Map<?, ConnectionFactory> loadConnectionFactory() {
        return this.dynamicRoutingConnectionFactoryLoader.load();
    }

}

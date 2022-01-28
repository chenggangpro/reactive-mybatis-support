package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import io.r2dbc.spi.ConnectionFactoryOptions;

/**
 * ConnectionFactoryOptions customizer
 *
 * @author Gang Cheng
 * @version 1.0.3
 * @since 1.0.3
 */
@FunctionalInterface
public interface ConnectionFactoryOptionsCustomizer {

    /**
     * customize ConnectionFactoryOptions
     *
     * @param connectionFactoryOptionsBuilder the original ConnectionFactoryOptions.Builder
     */
    void customize(ConnectionFactoryOptions.Builder connectionFactoryOptionsBuilder);
}

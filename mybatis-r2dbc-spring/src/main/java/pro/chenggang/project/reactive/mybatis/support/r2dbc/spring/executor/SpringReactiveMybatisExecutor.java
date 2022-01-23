package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor;

import io.r2dbc.spi.Connection;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.DefaultReactiveMybatisExecutor;
import reactor.core.publisher.Mono;

/**
 * The type Spring reactive mybatis executor.
 * override closeConnection with {@link ConnectionFactoryUtils}
 * <p>
 * {@link org.springframework.r2dbc.core.DatabaseClient}
 *
 * @author Gang Cheng
 * @since 1.0.0
 */
public class SpringReactiveMybatisExecutor extends DefaultReactiveMybatisExecutor {

    /**
     * Instantiates a new Spring reactive mybatis executor.
     *
     * @param configuration the configuration
     */
    public SpringReactiveMybatisExecutor(R2dbcMybatisConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Mono<Void> closeConnection(Connection connection) {
        return ConnectionFactoryUtils.currentConnectionFactory(connectionFactory)
                .then()
                .onErrorResume(Exception.class, e -> Mono.from(connection.close()));
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor;

import io.r2dbc.spi.Connection;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.DefaultReactiveMybatisExecutor;
import reactor.core.publisher.Mono;

/**
 * @author: chenggang
 * @date 12/16/21.
 */
public class SpringReactiveMybatisExecutor extends DefaultReactiveMybatisExecutor {

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

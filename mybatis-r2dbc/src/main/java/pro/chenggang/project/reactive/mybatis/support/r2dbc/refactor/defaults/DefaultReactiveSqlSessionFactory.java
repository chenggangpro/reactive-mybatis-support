package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.defaults;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.IsolationLevel;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.DefaultReactiveExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.ReactiveExecutor;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class DefaultReactiveSqlSessionFactory implements ReactiveSqlSessionFactory {

    private final R2dbcConfiguration configuration;

    public DefaultReactiveSqlSessionFactory(R2dbcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ReactiveSqlSession openSession(boolean autoCommit) {
        ReactiveExecutor reactiveExecutor = new DefaultReactiveExecutor(this.configuration,this.configuration.getConnectionFactory());
        return new DefaultReactiveSqlSession(this.configuration,reactiveExecutor,autoCommit, null);
    }

    @Override
    public ReactiveSqlSession openSession(IsolationLevel level) {
        ReactiveExecutor reactiveExecutor = new DefaultReactiveExecutor(this.configuration,this.configuration.getConnectionFactory());
        return new DefaultReactiveSqlSession(this.configuration,reactiveExecutor,false, level);
    }

    @Override
    public R2dbcConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void close() throws Exception {
        if(this.configuration.getConnectionFactory() instanceof ConnectionPool){
            ConnectionPool connectionPool = ((ConnectionPool) this.configuration.getConnectionFactory());
            if (!connectionPool.isDisposed()) {
                connectionPool.dispose();
            }
        }
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.IsolationLevel;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.connection.DefaultTransactionSupportConnectionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.DefaultReactiveExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveExecutor;

import java.io.Closeable;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class DefaultReactiveSqlSessionFactory implements ReactiveSqlSessionFactory {

    private final R2dbcMybatisConfiguration configuration;

    public DefaultReactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        if(connectionFactory instanceof ConnectionPool){
            ConnectionFactory transactionSupportConnectionFactory = new DefaultTransactionSupportConnectionFactory(connectionFactory);
            this.configuration.setConnectionFactory(transactionSupportConnectionFactory);
        }
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
    public R2dbcMybatisConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void close() throws Exception {
        if(this.configuration.getConnectionFactory() instanceof Closeable){
            Closeable closeableConnectionFactory = ((Closeable) this.configuration.getConnectionFactory());
            closeableConnectionFactory.close();
        }
    }
}

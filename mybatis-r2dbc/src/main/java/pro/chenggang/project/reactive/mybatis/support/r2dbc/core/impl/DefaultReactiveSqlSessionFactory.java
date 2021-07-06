package pro.chenggang.project.reactive.mybatis.support.r2dbc.core.impl;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.core.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.R2dbcMybatisConfiguration;

import java.io.IOException;

/**
 * Default ReactiveSqlSessionFactory
 * copy from https://github.com/linux-china/mybatis-r2dbc
 * @author linux_china
 */
public class DefaultReactiveSqlSessionFactory implements ReactiveSqlSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultReactiveSqlSessionFactory.class);

    private final R2dbcMybatisConfiguration configuration;
    private final ConnectionFactory connectionFactory;
    private final ReactiveSqlSession sqlSession;

    public DefaultReactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
        this.sqlSession = new DefaultReactiveSqlSession(configuration, this.connectionFactory);
    }

    @Override
    public ReactiveSqlSession openSession() {
        return this.sqlSession;
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    @Override
    public void close() throws IOException {
        if (this.connectionFactory instanceof ConnectionPool) {
            ConnectionPool connectionPool = ((ConnectionPool) this.connectionFactory);
            if (!connectionPool.isDisposed()) {
                connectionPool.dispose();
            }
        }
    }

}

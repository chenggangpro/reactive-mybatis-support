package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * @author: chenggang
 * @date 12/7/21.
 */
public interface ReactiveSqlSessionFactory extends AutoCloseable {

    /**
     * open session
     * @return
     */
    ReactiveSqlSession openSession();

    /**
     * get configuration
     * @return
     */
    R2dbcMybatisConfiguration getConfiguration();
}

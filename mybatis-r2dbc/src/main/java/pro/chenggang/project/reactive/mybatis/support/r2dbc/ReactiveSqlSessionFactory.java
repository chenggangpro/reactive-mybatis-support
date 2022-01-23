package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * The interface Reactive sql session factory.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface ReactiveSqlSessionFactory extends AutoCloseable {

    /**
     * open session
     *
     * @return reactive sql session
     */
    ReactiveSqlSession openSession();

    /**
     * get R2dbcMybatisConfiguration
     *
     * @return configuration
     */
    R2dbcMybatisConfiguration getConfiguration();
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import io.r2dbc.spi.IsolationLevel;
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
    default ReactiveSqlSession openSession(){
        return openSession(true);
    }

    /**
     * open session
     * @param autoCommit
     * @return
     */
    ReactiveSqlSession openSession(boolean autoCommit);

    /**
     * open session
     * @param level
     * @return
     */
    ReactiveSqlSession openSession(IsolationLevel level);

    /**
     * get configuration
     * @return
     */
    R2dbcMybatisConfiguration getConfiguration();
}

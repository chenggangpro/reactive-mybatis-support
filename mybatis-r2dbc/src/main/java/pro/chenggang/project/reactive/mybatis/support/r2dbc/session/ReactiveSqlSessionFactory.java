package pro.chenggang.project.reactive.mybatis.support.r2dbc.session;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.session.Configuration;

/**
 * reactive SQL session factory
 * copy from https://github.com/linux-china/mybatis-r2dbc
 * @author linux_china
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
    Configuration getConfiguration();

    /**
     * get connection factory
     * @return
     */
    ConnectionFactory getConnectionFactory();
}

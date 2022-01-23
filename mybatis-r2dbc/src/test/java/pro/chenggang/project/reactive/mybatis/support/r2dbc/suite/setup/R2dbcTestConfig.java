package pro.chenggang.project.reactive.mybatis.support.r2dbc.suite.setup;

import java.time.Duration;

/**
 * @author Gang Cheng
 */
public class R2dbcTestConfig {

    protected Integer initialSize = 1;
    protected Integer maxSize = 3;
    protected Duration maxIdleTime = Duration.ofMinutes(30);
    protected String databaseIp = "127.0.0.1";
    protected String databasePort = "3306";
    protected String databaseName = "r2dbc";
    protected String databaseUsername = "root";
    protected String databasePassword = "123456";
}

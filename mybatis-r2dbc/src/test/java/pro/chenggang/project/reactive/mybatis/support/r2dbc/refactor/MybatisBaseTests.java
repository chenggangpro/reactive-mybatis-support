package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.properties.R2dbcConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.properties.R2dbcMybatisProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import reactor.core.publisher.Hooks;

import java.time.Duration;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * @author: chenggang
 * @date 7/11/21.
 */
@TestInstance(PER_CLASS)
public class MybatisBaseTests {

    protected R2dbcMybatisProperties r2dbcMybatisProperties;
    protected R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties;
    protected R2dbcConfiguration r2dbcMybatisConfiguration;
    protected ConnectionFactory connectionFactory;
    protected ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    @BeforeAll
    public void setUp() throws Exception{
        Hooks.onOperatorDebug();
        this.r2dbcMybatisProperties = this.r2dbcMybatisProperties();
        this.r2dbcConnectionFactoryProperties = this.r2dbcConnectionFactoryProperties();
        this.r2dbcMybatisConfiguration = this.configuration(this.r2dbcMybatisProperties);
        this.connectionFactory = this.connectionFactory(this.r2dbcConnectionFactoryProperties);
        this.reactiveSqlSessionFactory = this.reactiveSqlSessionFactory(this.r2dbcMybatisConfiguration,this.connectionFactory);
    }

    public R2dbcMybatisProperties r2dbcMybatisProperties(){
        R2dbcMybatisProperties r2dbcMybatisProperties = new R2dbcMybatisProperties();
        r2dbcMybatisProperties.setMapperLocations(new String[]{"classpath:mapper/*.xml"});
        r2dbcMybatisProperties.setMapUnderscoreToCamelCase(true);
        r2dbcMybatisProperties.setTypeAliasesPackage("pro.chenggang.project.reactive.mybatis.support.r2dbc.application.model");
        return r2dbcMybatisProperties;
    }

    public R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties(){
        R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties = new R2dbcConnectionFactoryProperties();
        r2dbcConnectionFactoryProperties.setEnableMetrics(true);
        r2dbcConnectionFactoryProperties.setName("test-r2dbc");
        r2dbcConnectionFactoryProperties.setJdbcUrl("r2dbc:mysql://127.0.0.1:3306/mac");
        r2dbcConnectionFactoryProperties.setUsername("root");
        r2dbcConnectionFactoryProperties.setPassword("123456");
        R2dbcConnectionFactoryProperties.Pool pool = new R2dbcConnectionFactoryProperties.Pool();
        pool.setMaxIdleTime(Duration.parse("PT5M"));
        pool.setValidationQuery("SELECT 1 FROM DUAL");
        pool.setInitialSize(1);
        pool.setMaxSize(3);
        r2dbcConnectionFactoryProperties.setPool(pool);
        return r2dbcConnectionFactoryProperties;
    }


    public R2dbcConfiguration configuration(R2dbcMybatisProperties properties) {
        R2dbcConfiguration configuration = new R2dbcConfiguration();
        if (properties.getTypeAliasesPackage() != null) {
            String[] typeAliasPackageArray = tokenizeToStringArray(properties.getTypeAliasesPackage(),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeAliasPackageArray) {
                configuration.getTypeAliasRegistry().registerAliases(packageToScan, Object.class);
            }
        }
        Resource[] mapperLocations = properties.resolveMapperLocations();
        if(mapperLocations != null && mapperLocations.length > 0) {
            for (Resource mapperLocation : mapperLocations) {
                if (mapperLocation == null) {
                    continue;
                }
                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        } else {
            throw new IllegalArgumentException("mapperLocations cannot be empty...");
        }
        return configuration;
    }

    public ConnectionPool connectionFactory(R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties){
        ConnectionFactory connectionFactory = ConnectionFactories.get(r2dbcConnectionFactoryProperties.determineConnectionFactoryUrl());
        if (connectionFactory instanceof ConnectionPool) {
            return (ConnectionPool) connectionFactory;
        }
        R2dbcConnectionFactoryProperties.Pool pool = r2dbcConnectionFactoryProperties.getPool();
        ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
                .name(r2dbcConnectionFactoryProperties.determineConnectionFactoryName())
                .maxSize(pool.getMaxSize())
                .initialSize(pool.getInitialSize())
                .maxIdleTime(pool.getMaxIdleTime())
                .acquireRetry(pool.getAcquireRetry())
                .backgroundEvictionInterval(pool.getBackgroundEvictionInterval())
                .maxAcquireTime(pool.getMaxAcquireTime())
                .maxCreateConnectionTime(pool.getMaxCreateConnectionTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .validationDepth(pool.getValidationDepth());
        if(pool.getValidationQuery() != null){
            builder.validationQuery(pool.getValidationQuery());
        }else{
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        ConnectionPool connectionPool = new ConnectionPool(builder.build());
        return connectionPool;
    }

    public ReactiveSqlSessionFactory reactiveSqlSessionFactory(R2dbcConfiguration configuration, ConnectionFactory connectionFactory) {
        configuration.setConnectionFactory(connectionFactory);
        return new DefaultReactiveSqlSessionFactory(configuration);
    }
}

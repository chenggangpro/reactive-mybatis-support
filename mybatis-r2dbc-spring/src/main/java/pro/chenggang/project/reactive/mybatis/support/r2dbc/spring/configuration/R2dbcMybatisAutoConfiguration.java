package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.configuration;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.connectionfactory.TransactionAwareConnectionFactoryProxy;
import org.springframework.transaction.ReactiveTransactionManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.properties.R2dbcConnectionFactoryProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.properties.R2dbcConnectionFactoryProperties.Pool;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.properties.R2dbcMybatisProperties;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.session.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.session.defaults.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.R2dbcAutoConfiguredMapperScannerRegistrar;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.R2dbcMapperScannerRegistrar;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.executor.SpringR2dbcReactiveSqlSessionExecutor;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveSqlSessionExecutor;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * R2dbc Mybatis Auto Configuration
 * @author evans
 */
@Slf4j
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import({ R2dbcAutoConfiguredMapperScannerRegistrar.class,R2dbcMapperScannerRegistrar.class})
@ConditionalOnClass(ConnectionFactory.class)
public class R2dbcMybatisAutoConfiguration {

    @ConfigurationProperties(R2dbcMybatisProperties.PREFIX)
    @Bean
    public R2dbcMybatisProperties r2dbcMybatisProperties(){
        return new R2dbcMybatisProperties();
    }

    @ConfigurationProperties(R2dbcConnectionFactoryProperties.PREFIX)
    @Bean
    public R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties(){
        return new R2dbcConnectionFactoryProperties();
    }

    @Bean
    public R2dbcMybatisConfiguration configuration(R2dbcMybatisProperties properties) {
        R2dbcMybatisConfiguration configuration = new R2dbcMybatisConfiguration();
        if (hasText(properties.getTypeAliasesPackage())) {
            String[] typeAliasPackageArray = tokenizeToStringArray(properties.getTypeAliasesPackage(),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeAliasPackageArray) {
                configuration.getTypeAliasRegistry().registerAliases(packageToScan, Object.class);
            }
        } else {
            log.info("Type Alias Package Is Empty");
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
        configuration.initR2dbcTypeHandler();
        return configuration;
    }

    @ConditionalOnMissingBean(ConnectionFactory.class)
    @Bean(destroyMethod = "dispose")
    public ConnectionPool connectionFactory(R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties){
        ConnectionFactory connectionFactory = ConnectionFactories.get(r2dbcConnectionFactoryProperties.determineConnectionFactoryUrl());
        if (connectionFactory instanceof ConnectionPool) {
            return (ConnectionPool) connectionFactory;
        }
        Pool pool = r2dbcConnectionFactoryProperties.getPool();
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
        if(hasText(pool.getValidationQuery())){
            builder.validationQuery(pool.getValidationQuery());
        }else{
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        ConnectionPool connectionPool = new ConnectionPool(builder.build());
        log.info("Initial Connection Pool Bean Success");
        return connectionPool;
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveTransactionManager.class)
    public R2dbcTransactionManager connectionFactoryTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveSqlSessionExecutor.class)
    public ReactiveSqlSessionExecutor reactiveSqlSessionExecutor(){
        return new SpringR2dbcReactiveSqlSessionExecutor();
    }


    @Bean
    @ConditionalOnMissingBean(ReactiveSqlSessionFactory.class)
    public ReactiveSqlSessionFactory reactiveSqlSessionFactoryWithTransaction(R2dbcMybatisConfiguration configuration, ConnectionFactory connectionFactory,ReactiveSqlSessionExecutor reactiveSqlSessionExecutor) {
        return new DefaultReactiveSqlSessionFactory(configuration, new TransactionAwareConnectionFactoryProxy(connectionFactory),reactiveSqlSessionExecutor);
    }

}

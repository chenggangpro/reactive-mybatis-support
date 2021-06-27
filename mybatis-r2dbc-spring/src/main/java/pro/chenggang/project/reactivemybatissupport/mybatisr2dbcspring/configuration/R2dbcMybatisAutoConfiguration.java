package pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.configuration;

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
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.ReactiveSqlSessionFactory;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.impl.DefaultReactiveSqlSessionFactory;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.support.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.properties.R2dbcConnectionFactoryProperties;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.properties.R2dbcMybatisProperties;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.support.R2dbcAutoConfiguredMapperScannerRegistrar;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.support.R2dbcMapperScannerRegistrar;

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
        return configuration;
    }

    @ConditionalOnMissingBean(ConnectionFactory.class)
    @Bean(destroyMethod = "dispose")
    public ConnectionPool connectionFactory(R2dbcConnectionFactoryProperties r2dbcConnectionFactoryProperties){
        ConnectionFactory connectionFactory = ConnectionFactories.get(r2dbcConnectionFactoryProperties.determineConnectionFactoryUrl());
        if (connectionFactory instanceof ConnectionPool) {
            return (ConnectionPool) connectionFactory;
        }
        ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory)
                .name(r2dbcConnectionFactoryProperties.determineConnectionFactoryName())
                .maxSize(r2dbcConnectionFactoryProperties.getMaxSize())
                .initialSize(r2dbcConnectionFactoryProperties.getInitialSize())
                .maxIdleTime(r2dbcConnectionFactoryProperties.getMaxIdleTime())
                .acquireRetry(r2dbcConnectionFactoryProperties.getAcquireRetry())
                .backgroundEvictionInterval(r2dbcConnectionFactoryProperties.getBackgroundEvictionInterval())
                .maxAcquireTime(r2dbcConnectionFactoryProperties.getMaxAcquireTime())
                .maxCreateConnectionTime(r2dbcConnectionFactoryProperties.getMaxCreateConnectionTime())
                .maxLifeTime(r2dbcConnectionFactoryProperties.getMaxLifeTime())
                .validationDepth(r2dbcConnectionFactoryProperties.getValidationDepth());
        if(hasText(r2dbcConnectionFactoryProperties.getValidationQuery())){
            builder.validationQuery(r2dbcConnectionFactoryProperties.getValidationQuery());
        }else{
            builder.validationDepth(ValidationDepth.LOCAL);
        }
        ConnectionPool connectionPool = new ConnectionPool(builder.build());
        log.info("Initial Connection Pool Bean Success");
        return connectionPool;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveSqlSessionFactory reactiveSqlSessionFactory(R2dbcMybatisConfiguration configuration, ConnectionFactory connectionFactory) {
        return new DefaultReactiveSqlSessionFactory(configuration, connectionFactory);
    }

}

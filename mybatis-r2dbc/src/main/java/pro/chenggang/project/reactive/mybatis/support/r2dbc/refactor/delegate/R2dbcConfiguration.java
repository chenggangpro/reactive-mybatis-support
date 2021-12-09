package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.session.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapterRegistry;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class R2dbcConfiguration extends Configuration {

    private final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);
    private final JdbcParameterAdapterRegistry jdbcParameterAdapterRegistry = new JdbcParameterAdapterRegistry(this);
    private ConnectionFactory connectionFactory;

    public <T> T getMapper(Class<T> type, ReactiveSqlSession reactiveSqlSession) {
        return r2dbcMapperRegistry.getMapper(type, reactiveSqlSession);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        this.r2dbcMapperRegistry.addMapper(type);
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        this.r2dbcMapperRegistry.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        this.r2dbcMapperRegistry.addMappers(packageName);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return this.r2dbcMapperRegistry.hasMapper(type);
    }

    public void addJdbcParameterAdapter(JdbcParameterAdapter jdbcParameterAdapter){
        this.jdbcParameterAdapterRegistry.register(jdbcParameterAdapter);
    }

    public void addJdbcParameterAdapter(String packageName){
        this.jdbcParameterAdapterRegistry.register(packageName);
    }

    public JdbcParameterAdapterRegistry getJdbcParameterAdapterRegistry() {
        return jdbcParameterAdapterRegistry;
    }
}

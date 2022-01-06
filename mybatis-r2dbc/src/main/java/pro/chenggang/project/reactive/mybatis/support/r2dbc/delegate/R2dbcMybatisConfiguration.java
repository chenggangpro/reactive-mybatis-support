package pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLXML;
import java.util.HashSet;
import java.util.Set;

/**
 * The type R2dbc mybatis configuration.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12 /8/21.
 */
public class R2dbcMybatisConfiguration extends Configuration {

    private final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);
    private final R2dbcTypeHandlerAdapterRegistry r2dbcTypeHandlerAdapterRegistry = new R2dbcTypeHandlerAdapterRegistry(this);
    private final Set<Class<?>> notSupportedDataTypes = new HashSet<>();
    private ConnectionFactory connectionFactory;

    /**
     * Instantiates a new R2dbc mybatis configuration.
     */
    public R2dbcMybatisConfiguration() {
        this.loadNotSupportedDataTypes();
    }

    /**
     * Instantiates a new R2dbc mybatis configuration.
     *
     * @param environment the environment
     */
    public R2dbcMybatisConfiguration(Environment environment) {
        super(environment);
        this.loadNotSupportedDataTypes();
    }

    /**
     * load not supported data types
     */
    private void loadNotSupportedDataTypes() {
        this.notSupportedDataTypes.add(InputStream.class);
        this.notSupportedDataTypes.add(SQLXML.class);
        this.notSupportedDataTypes.add(Reader.class);
        this.notSupportedDataTypes.add(StringReader.class);
    }

    /**
     * Gets mapper.
     *
     * @param <T>                the type parameter
     * @param type               the type
     * @param reactiveSqlSession the reactive sql session
     * @return the mapper
     */
    public <T> T getMapper(Class<T> type, ReactiveSqlSession reactiveSqlSession) {
        return r2dbcMapperRegistry.getMapper(type, reactiveSqlSession);
    }

    /**
     * Gets connection factory.
     *
     * @return the connection factory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets connection factory.
     *
     * @param connectionFactory the connection factory
     */
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

    /**
     * Add R2dbc type handler adapter.
     *
     * @param r2dbcTypeHandlerAdapter the R2dbc type handler adapter
     */
    public void addR2dbcTypeHandlerAdapter(R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter) {
        this.r2dbcTypeHandlerAdapterRegistry.register(r2dbcTypeHandlerAdapter);
    }

    /**
     * Add R2dbc type handler adapter.
     *
     * @param packageName the package name
     */
    public void addR2dbcTypeHandlerAdapter(String packageName) {
        this.r2dbcTypeHandlerAdapterRegistry.register(packageName);
    }

    /**
     * Gets R2dbc type handler adapter registry.
     *
     * @return the R2dbc type handler adapter registry
     */
    public R2dbcTypeHandlerAdapterRegistry getR2dbcTypeHandlerAdapterRegistry() {
        return r2dbcTypeHandlerAdapterRegistry;
    }

    /**
     * Sets not supported jdbc type.
     *
     * @param clazz the clazz
     */
    public void setNotSupportedJdbcType(Class<?> clazz) {
        this.notSupportedDataTypes.add(clazz);
    }

    /**
     * Gets not supported data types.
     *
     * @return the not supported data types
     */
    public Set<Class<?>> getNotSupportedDataTypes() {
        return this.notSupportedDataTypes;
    }
}

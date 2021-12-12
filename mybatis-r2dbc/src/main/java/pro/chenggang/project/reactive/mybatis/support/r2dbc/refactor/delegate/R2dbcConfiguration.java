package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate;

import io.r2dbc.spi.ConnectionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.ParameterHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.ParameterHandlerAdapterRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapterRegistry;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLXML;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class R2dbcConfiguration extends Configuration {

    private final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);
    private final ParameterHandlerAdapterRegistry parameterHandlerAdapterRegistry = new ParameterHandlerAdapterRegistry(this);
    private final ResultHandlerAdapterRegistry resultHandlerAdapterRegistry = new ResultHandlerAdapterRegistry(this);
    private final Set<Class> notSupportedDataTypes = new HashSet<>();
    private ConnectionFactory connectionFactory;

    public R2dbcConfiguration() {
        this.loadNotSupportedDataTypes();
    }

    public R2dbcConfiguration(Environment environment) {
        super(environment);
        this.loadNotSupportedDataTypes();
    }

    /**
     * load not supported data types
     */
    private void loadNotSupportedDataTypes(){
        this.notSupportedDataTypes.add(InputStream.class);
        this.notSupportedDataTypes.add(SQLXML.class);
        this.notSupportedDataTypes.add(Reader.class);
        this.notSupportedDataTypes.add(StringReader.class);
    }

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

    public void addParameterHandlerAdapter(ParameterHandlerAdapter parameterHandlerAdapter){
        this.parameterHandlerAdapterRegistry.register(parameterHandlerAdapter);
    }

    public void addParameterHandlerAdapter(String packageName){
        this.parameterHandlerAdapterRegistry.register(packageName);
    }

    public ParameterHandlerAdapterRegistry getParameterHandlerAdapterRegistry() {
        return parameterHandlerAdapterRegistry;
    }

    public void addResultHandlerAdapter(ResultHandlerAdapter resultHandlerAdapter){
        this.resultHandlerAdapterRegistry.register(resultHandlerAdapter);
    }

    public void addResultHandlerAdapter(String packageName){
        this.resultHandlerAdapterRegistry.register(packageName);
    }

    public ResultHandlerAdapterRegistry getResultHandlerAdapterrRegistry() {
        return resultHandlerAdapterRegistry;
    }

    public void setNotSupportedJdbcType(Class clazz){
        this.notSupportedDataTypes.add(clazz);
    }

    public Set<Class> getNotSupportedDataTypes(){
        return this.notSupportedDataTypes;
    }
}

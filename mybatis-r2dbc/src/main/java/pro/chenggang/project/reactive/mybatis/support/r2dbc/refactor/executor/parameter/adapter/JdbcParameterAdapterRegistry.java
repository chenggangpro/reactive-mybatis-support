package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.JdbcParameterAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class JdbcParameterAdapterRegistry {

    private final Map<Class, JdbcParameterAdapter> jdbcParameterAdapterContainer = new HashMap<>();
    private final R2dbcConfiguration r2dbcConfiguration;

    public JdbcParameterAdapterRegistry(R2dbcConfiguration r2dbcConfiguration) {
        this.r2dbcConfiguration = r2dbcConfiguration;
        register(new ByteObjectArrayParameterAdapter());
        register(new ByteObjectArrayParameterAdapter());
        register(new OffsetDateTimeJdbcParameterAdapter());
        register(new OffsetTimeJdbcParameterAdapter());
        register(new SqlDateJdbcParameterAdapter());
        register(new SqlTimeJdbcParameterAdapter());
        register(new TimestampJdbcParameterAdapter());
        register(new ByteObjectArrayParameterAdapter());
        register(new ZonedDateTimeJdbcParameterAdapter());
    }

    public Map<Class, JdbcParameterAdapter> getAllJdbcParameterAdapters(){
        return this.jdbcParameterAdapterContainer;
    }

    public void register(JdbcParameterAdapter jdbcParameterAdapter){
        jdbcParameterAdapterContainer.put(jdbcParameterAdapter.adaptClazz(),jdbcParameterAdapter);
    }

    public void register(String packageName){
        ResolverUtil<JdbcParameterAdapter> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(JdbcParameterAdapter.class), packageName);
        resolverUtil.getClasses().forEach(clazz -> {
            ObjectFactory objectFactory = r2dbcConfiguration.getObjectFactory();
            JdbcParameterAdapter jdbcParameterAdapter = objectFactory.create(clazz);
            this.register(jdbcParameterAdapter);
        });

    }
}

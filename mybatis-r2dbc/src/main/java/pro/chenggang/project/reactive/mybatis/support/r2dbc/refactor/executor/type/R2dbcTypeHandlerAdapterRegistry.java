package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.type;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.type.defaults.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class R2dbcTypeHandlerAdapterRegistry {

    private final Map<Class, R2dbcTypeHandlerAdapter> r2dbcTypeHandlerAdapterContainer = new HashMap<>();
    private final R2dbcConfiguration r2dbcConfiguration;

    public R2dbcTypeHandlerAdapterRegistry(R2dbcConfiguration r2dbcConfiguration) {
        this.r2dbcConfiguration = r2dbcConfiguration;
        register(new ByteObjectArrayR2dbcTypeHandlerAdapter());
        register(new ByteObjectArrayR2dbcTypeHandlerAdapter());
        register(new OffsetDateTimeR2dbcTypeHandlerAdapter());
        register(new OffsetTimeR2dbcTypeHandlerAdapter());
        register(new SqlDateR2dbcTypeHandlerAdapter());
        register(new SqlTimeR2dbcTypeHandlerAdapter());
        register(new TimestampR2dbcTypeHandlerAdapter());
        register(new ByteObjectArrayR2dbcTypeHandlerAdapter());
        register(new ZonedDateTimeR2dbcTypeHandlerAdapter());
    }

    public Map<Class, R2dbcTypeHandlerAdapter> getR2dbcTypeHandlerAdapters(){
        return this.r2dbcTypeHandlerAdapterContainer;
    }

    public void register(R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter){
        r2dbcTypeHandlerAdapterContainer.put(r2dbcTypeHandlerAdapter.adaptClazz(), r2dbcTypeHandlerAdapter);
    }

    public void register(String packageName){
        ResolverUtil<R2dbcTypeHandlerAdapter> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(R2dbcTypeHandlerAdapter.class), packageName);
        resolverUtil.getClasses().forEach(clazz -> {
            ObjectFactory objectFactory = r2dbcConfiguration.getObjectFactory();
            R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter = objectFactory.create(clazz);
            this.register(r2dbcTypeHandlerAdapter);
        });

    }
}

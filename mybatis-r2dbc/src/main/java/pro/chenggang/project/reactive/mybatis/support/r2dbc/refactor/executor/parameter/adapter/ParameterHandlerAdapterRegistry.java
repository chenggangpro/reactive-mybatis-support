package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ParameterHandlerAdapterRegistry {

    private final Map<Class, ParameterHandlerAdapter> parameterHandlerAdapterContainer = new HashMap<>();
    private final R2dbcConfiguration r2dbcConfiguration;

    public ParameterHandlerAdapterRegistry(R2dbcConfiguration r2dbcConfiguration) {
        this.r2dbcConfiguration = r2dbcConfiguration;
        register(new ByteObjectArrayParameterHandlerAdapter());
        register(new ByteObjectArrayParameterHandlerAdapter());
        register(new OffsetDateTimeParameterHandlerAdapter());
        register(new OffsetTimeParameterHandlerAdapter());
        register(new SqlDateParameterHandlerAdapter());
        register(new SqlTimeParameterHandlerAdapter());
        register(new TimestampParameterHandlerAdapter());
        register(new ByteObjectArrayParameterHandlerAdapter());
        register(new ZonedDateTimeParameterHandlerAdapter());
    }

    public Map<Class, ParameterHandlerAdapter> getAllParameterHandlerAdapters(){
        return this.parameterHandlerAdapterContainer;
    }

    public void register(ParameterHandlerAdapter parameterHandlerAdapter){
        parameterHandlerAdapterContainer.put(parameterHandlerAdapter.adaptClazz(), parameterHandlerAdapter);
    }

    public void register(String packageName){
        ResolverUtil<ParameterHandlerAdapter> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(ParameterHandlerAdapter.class), packageName);
        resolverUtil.getClasses().forEach(clazz -> {
            ObjectFactory objectFactory = r2dbcConfiguration.getObjectFactory();
            ParameterHandlerAdapter parameterHandlerAdapter = objectFactory.create(clazz);
            this.register(parameterHandlerAdapter);
        });

    }
}

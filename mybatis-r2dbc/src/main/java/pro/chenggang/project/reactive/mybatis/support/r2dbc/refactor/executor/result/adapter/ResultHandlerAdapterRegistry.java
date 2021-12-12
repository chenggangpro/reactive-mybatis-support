package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate.R2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ResultHandlerAdapterRegistry {

    private final Map<Class, ResultHandlerAdapter> resultHandlerAdapterContainer = new HashMap<>();
    private final R2dbcConfiguration r2dbcConfiguration;

    public ResultHandlerAdapterRegistry(R2dbcConfiguration r2dbcConfiguration) {
        this.r2dbcConfiguration = r2dbcConfiguration;
        register(new ByteObjectArrayResultHandlerAdapter());
        register(new ByteObjectArrayResultHandlerAdapter());
        register(new OffsetDateTimeResultHandlerAdapter());
        register(new OffsetTimeResultHandlerAdapter());
        register(new SqlDateResultHandlerAdapter());
        register(new SqlTimeResultHandlerAdapter());
        register(new TimestampResultHandlerAdapter());
        register(new ByteObjectArrayResultHandlerAdapter());
        register(new ZonedDateTimeResultHandlerAdapter());
    }

    public Map<Class, ResultHandlerAdapter> getAllResultHandlerAdapters(){
        return this.resultHandlerAdapterContainer;
    }

    public void register(ResultHandlerAdapter resultHandlerAdapter){
        resultHandlerAdapterContainer.put(resultHandlerAdapter.adaptClazz(), resultHandlerAdapter);
    }

    public void register(String packageName){
        ResolverUtil<ResultHandlerAdapter> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(ResultHandlerAdapter.class), packageName);
        resolverUtil.getClasses().forEach(clazz -> {
            ObjectFactory objectFactory = r2dbcConfiguration.getObjectFactory();
            ResultHandlerAdapter resultHandlerAdapter = objectFactory.create(clazz);
            this.register(resultHandlerAdapter);
        });

    }
}

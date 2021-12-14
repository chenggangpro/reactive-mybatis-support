package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lasse Voss
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;
    private final Map<Method, MapperProxy.MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public Map<Method, MapperProxy.MapperMethodInvoker> getMethodCache() {
        return methodCache;
    }

    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                mapperInterface,
                () -> mapperProxy
        );
    }

    public T newInstance(ReactiveSqlSession reactiveSqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(reactiveSqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}

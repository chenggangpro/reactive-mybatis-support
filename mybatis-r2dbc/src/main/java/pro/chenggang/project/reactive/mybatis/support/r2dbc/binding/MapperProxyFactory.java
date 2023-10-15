/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.binding;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.support.ProxyInstanceFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Mapper proxy factory.
 *
 * @param <T> the type parameter
 * @author Lasse Voss
 * @author Gang Cheng
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;
    private final Map<Method, MapperProxy.MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Mapper proxy factory.
     *
     * @param mapperInterface the mapper interface
     */
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * Gets mapper interface.
     *
     * @return the mapper interface
     */
    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    /**
     * Gets method cache.
     *
     * @return the method cache
     */
    public Map<Method, MapperProxy.MapperMethodInvoker> getMethodCache() {
        return methodCache;
    }

    /**
     * New instance t.
     *
     * @param mapperProxy the mapper proxy
     * @return the t
     */
    protected T newInstance(MapperProxy<T> mapperProxy) {
        return ProxyInstanceFactory.newInstanceOfInterfaces(
                mapperInterface,
                () -> mapperProxy
        );
    }

    /**
     * New instance t.
     *
     * @param reactiveSqlSession the reactive sql session
     * @return the t
     */
    public T newInstance(ReactiveSqlSession reactiveSqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(reactiveSqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}

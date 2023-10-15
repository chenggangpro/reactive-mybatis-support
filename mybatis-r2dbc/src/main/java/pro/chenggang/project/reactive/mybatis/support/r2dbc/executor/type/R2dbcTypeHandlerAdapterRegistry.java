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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.ByteArrayR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.ByteObjectArrayR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.OffsetDateTimeR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.OffsetTimeR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.SqlDateR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.SqlTimeR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.TimestampR2dbcTypeHandlerAdapter;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults.ZonedDateTimeR2dbcTypeHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * The type R2dbc type handler adapter registry.
 *
 * @author Gang Cheng
 */
public class R2dbcTypeHandlerAdapterRegistry {

    private final Map<Class<?>, R2dbcTypeHandlerAdapter> r2dbcTypeHandlerAdapterContainer = new HashMap<>();
    private final R2dbcMybatisConfiguration r2DbcMybatisConfiguration;

    /**
     * Instantiates a new R2dbc type handler adapter registry.
     *
     * @param r2DbcMybatisConfiguration the r2dbc mybatis configuration
     */
    public R2dbcTypeHandlerAdapterRegistry(R2dbcMybatisConfiguration r2DbcMybatisConfiguration) {
        this.r2DbcMybatisConfiguration = r2DbcMybatisConfiguration;
        register(new ByteArrayR2dbcTypeHandlerAdapter());
        register(new ByteObjectArrayR2dbcTypeHandlerAdapter());
        register(new OffsetDateTimeR2dbcTypeHandlerAdapter());
        register(new OffsetTimeR2dbcTypeHandlerAdapter());
        register(new SqlDateR2dbcTypeHandlerAdapter());
        register(new SqlTimeR2dbcTypeHandlerAdapter());
        register(new TimestampR2dbcTypeHandlerAdapter());
        register(new ZonedDateTimeR2dbcTypeHandlerAdapter());
    }

    /**
     * Get r2dbc type handler adapters map.
     *
     * @return the map
     */
    public Map<Class<?>, R2dbcTypeHandlerAdapter> getR2dbcTypeHandlerAdapters() {
        return this.r2dbcTypeHandlerAdapterContainer;
    }

    /**
     * Register with r2dbcTypeHandlerAdapter
     *
     * @param r2dbcTypeHandlerAdapter the r2dbc type handler adapter
     */
    public void register(R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter) {
        r2dbcTypeHandlerAdapterContainer.put(r2dbcTypeHandlerAdapter.adaptClazz(), r2dbcTypeHandlerAdapter);
    }

    /**
     * Register with r2dbcTypeHandlerAdapter's Class
     *
     * @param r2dbcTypeHandlerAdapterClass the r2dbc type handler adapter class
     */
    public void register(Class<? extends R2dbcTypeHandlerAdapter> r2dbcTypeHandlerAdapterClass) {
        ObjectFactory objectFactory = r2DbcMybatisConfiguration.getObjectFactory();
        R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter = objectFactory.create(r2dbcTypeHandlerAdapterClass);
        this.register(r2dbcTypeHandlerAdapter);
    }

    /**
     * Register from package
     *
     * @param packageName the package name
     */
    public void register(String packageName) {
        ResolverUtil<R2dbcTypeHandlerAdapter> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(R2dbcTypeHandlerAdapter.class), packageName);
        resolverUtil.getClasses().forEach(clazz -> {
            ObjectFactory objectFactory = r2DbcMybatisConfiguration.getObjectFactory();
            R2dbcTypeHandlerAdapter r2dbcTypeHandlerAdapter = objectFactory.create(clazz);
            this.register(r2dbcTypeHandlerAdapter);
        });

    }
}

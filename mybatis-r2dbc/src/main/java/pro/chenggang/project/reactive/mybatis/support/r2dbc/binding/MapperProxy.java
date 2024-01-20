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

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.util.MapUtil;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The Mapper proxy.
 *
 * @param <T> the type parameter
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Gang Cheng
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -4724728412955527868L;
    private static final int ALLOWED_MODES = Lookup.PRIVATE | Lookup.PROTECTED
            | Lookup.PACKAGE | Lookup.PUBLIC;
    private static final Constructor<Lookup> lookupConstructor;
    private static final Method privateLookupInMethod;

    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
        } catch (NoSuchMethodException e) {
            privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            // JDK 1.8
            try {
                lookup = Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method " +
                                "in java.lang.invoke.MethodHandles.",
                        e
                );
            } catch (Exception e) {
                lookup = null;
            }
        }
        lookupConstructor = lookup;
    }

    private final ReactiveSqlSession reactiveSqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethodInvoker> methodCache;

    /**
     * Instantiates a new Mapper proxy.
     *
     * @param reactiveSqlSession the reactive sql session
     * @param mapperInterface    the mapper interface
     * @param methodCache        the method cache
     */
    public MapperProxy(ReactiveSqlSession reactiveSqlSession,
                       Class<T> mapperInterface,
                       Map<Method, MapperMethodInvoker> methodCache) {
        this.reactiveSqlSession = reactiveSqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            return cachedInvoker(method).invoke(proxy, method, args, reactiveSqlSession);
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
        try {
            return MapUtil.computeIfAbsent(methodCache, method, m -> {
                if (m.isDefault()) {
                    try {
                        if (privateLookupInMethod == null) {
                            return new DefaultMethodInvoker(getMethodHandleJava8(method));
                        }
                        return new DefaultMethodInvoker(getMethodHandleJava9(method));
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                            | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return new PlainMethodInvoker(new MapperMethod(mapperInterface,
                            method,
                            reactiveSqlSession.getConfiguration()
                    ));
                }
            });
        } catch (RuntimeException re) {
            Throwable cause = re.getCause();
            throw cause == null ? re : cause;
        }
    }

    private MethodHandle getMethodHandleJava9(Method method)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return ((Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
                declaringClass,
                method.getName(),
                MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                declaringClass
        );
    }

    private MethodHandle getMethodHandleJava8(Method method)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES)
                .unreflectSpecial(method, declaringClass);
    }

    /**
     * The interface Mapper method invoker.
     */
    interface MapperMethodInvoker {
        /**
         * Invoke object.
         *
         * @param proxy      the proxy
         * @param method     the method
         * @param args       the args
         * @param sqlSession the sql session
         * @return the object
         * @throws Throwable the throwable
         */
        Object invoke(Object proxy, Method method, Object[] args, ReactiveSqlSession sqlSession) throws Throwable;
    }

    private static class PlainMethodInvoker implements MapperMethodInvoker {
        private final MapperMethod mapperMethod;

        /**
         * Instantiates a new Plain method invoker.
         *
         * @param mapperMethod the mapper method
         */
        public PlainMethodInvoker(MapperMethod mapperMethod) {
            super();
            this.mapperMethod = mapperMethod;
        }

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args,
                             ReactiveSqlSession sqlSession) throws Throwable {
            return mapperMethod.execute(sqlSession, args);
        }
    }

    private static class DefaultMethodInvoker implements MapperMethodInvoker {

        private final MethodHandle methodHandle;

        /**
         * Instantiates a new Default method invoker.
         *
         * @param methodHandle the method handle
         */
        public DefaultMethodInvoker(MethodHandle methodHandle) {
            super();
            this.methodHandle = methodHandle;
        }

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args,
                             ReactiveSqlSession sqlSession) throws Throwable {
            return methodHandle.bindTo(proxy).invokeWithArguments(args);
        }
    }
}

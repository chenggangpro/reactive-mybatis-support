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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.support;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The type Proxy instance factory.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
@SuppressWarnings("unchecked")
public class ProxyInstanceFactory {

    /**
     * new instance of target class
     *
     * @param <T>                       the type parameter
     * @param interfaceType             the interface type
     * @param invocationHandlerSupplier the invocation handler supplier
     * @param otherInterfaces           the other interfaces
     * @return t
     * @throws IllegalStateException when unable create target interface Proxy Class
     */
    public static <T> T newInstanceOfInterfaces(Class<T> interfaceType, Supplier<InvocationHandler> invocationHandlerSupplier, Class<?>... otherInterfaces) {
        List<Class<?>> targetInterfaces = new ArrayList<>();
        targetInterfaces.add(interfaceType);
        if (null != otherInterfaces && otherInterfaces.length != 0) {
            targetInterfaces.addAll(Arrays.asList(otherInterfaces));
        }
        try {
            return (T) new ByteBuddy()
                    .subclass(Object.class)
                    .implement(targetInterfaces)
                    .intercept(InvocationHandlerAdapter.of(invocationHandlerSupplier.get()))
                    .make()
                    .load(interfaceType.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Unable create target interface Proxy Class", e);
        }
    }

}

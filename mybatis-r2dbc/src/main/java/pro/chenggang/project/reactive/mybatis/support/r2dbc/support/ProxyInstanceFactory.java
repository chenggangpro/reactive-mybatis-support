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

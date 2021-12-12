package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.support;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author: chenggang
 * @date 12/12/21.
 */
@SuppressWarnings("unchecked")
public class ProxyInstanceFactory {

    /**
     * new instance of
     * @param interfaceType
     * @param invocationHandlerSupplier
     * @param otherInterfaces
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static <T> T newInstanceOfInterfaces(Class<T> interfaceType, Supplier<InvocationHandler> invocationHandlerSupplier, Class ... otherInterfaces) {
        List<Class> targetInterfaces = new ArrayList<>();
        targetInterfaces.add(interfaceType);
        if(null != otherInterfaces && otherInterfaces.length != 0){
            targetInterfaces.addAll(Arrays.asList(otherInterfaces));
        }
        try{
            return (T) new ByteBuddy().subclass(Object.class)
                    .implement(targetInterfaces)
                    .intercept(InvocationHandlerAdapter.of(invocationHandlerSupplier.get()))
                    .make()
                    .load(interfaceType.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        }catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Unable create target interface Proxy Class",e);
        }
    }

}

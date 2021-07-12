package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invocation handler that suppresses close calls on R2DBC Connections.
 * (Prepared/CallbackStatement) objects.
 *
 * @see Connection#close()
 */
public class CloseSuppressingInvocationHandler implements InvocationHandler {

    private final Connection target;

    public CloseSuppressingInvocationHandler(Connection target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Invocation on ConnectionProxy interface coming in...
        if (method.getName().equals("equals")) {
            // Only consider equal when proxies are identical.
            return proxy == args[0];
        } else if (method.getName().equals("hashCode")) {
            // Use hashCode of PersistenceManager proxy.
            return System.identityHashCode(proxy);
        } else if (method.getName().equals("unwrap")) {
            return target;
        } else if (method.getName().equals("close")) {
            // Handle close method: suppress, not valid.
            return Mono.error(new UnsupportedOperationException("Close is not supported!"));
        } else if (method.getName().equals("getTargetConnection")) {
            // Handle getTargetConnection method: return underlying Connection.
            return this.target;
        }
        // Invoke method on target Connection.
        try {
            return method.invoke(this.target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
}
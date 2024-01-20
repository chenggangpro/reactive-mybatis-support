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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
class ProxyInstanceFactoryTest {

    @Test
    void newInstanceOfInterfaces() {
        TestInterface testInterface = ProxyInstanceFactory.newInstanceOfInterfaces(
                TestInterface.class,
                this::mockInvocationHandler
        );
        Assertions.assertNotNull(testInterface);
        Assertions.assertThrowsExactly(
                IllegalStateException.class,
                () -> {
                    TestClass testClass = ProxyInstanceFactory.newInstanceOfInterfaces(
                            TestClass.class,
                            this::mockInvocationHandler
                    );
                }
        );
    }

    public interface TestInterface {

    }

    public class TestClass {

    }

    private InvocationHandler mockInvocationHandler() {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(proxy, args);
            }
        };
    }
}
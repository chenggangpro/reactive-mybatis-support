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

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcMapperAnnotationBuilder;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.DefaultReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.DefaultReactiveMybatisExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class BindingSimpleTests {

    R2dbcMybatisConfiguration r2dbcMybatisConfiguration = new R2dbcMybatisConfiguration();
    ReactiveSqlSession mockReactiveSqlSession = new DefaultReactiveSqlSession(r2dbcMybatisConfiguration,
            new DefaultReactiveMybatisExecutor(r2dbcMybatisConfiguration),
            ReactiveSqlSession.DEFAULT_PROFILE
    );

    @Test
    void testWithBindingInterface() {
        MapperProxyFactory<BindingInterface> interfaceMapperProxyFactory = new MapperProxyFactory<>(
                BindingInterface.class
        );
        assertEquals(interfaceMapperProxyFactory.getMapperInterface(), BindingInterface.class);
        BindingInterface bindingInterface = interfaceMapperProxyFactory.newInstance(mockReactiveSqlSession);
        assertNotNull(bindingInterface);
    }

    @Test
    void testWithBindingClass() {
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
            MapperProxyFactory<BindingClass> interfaceMapperProxyFactory = new MapperProxyFactory<>(BindingClass.class);
            assertEquals(interfaceMapperProxyFactory.getMapperInterface(), BindingClass.class);
            BindingClass bindingClass = interfaceMapperProxyFactory.newInstance(mockReactiveSqlSession);
        });
    }

    @Test
    void testOkInterfaceMethod() {
        MapperProxyFactory<OkInterface> interfaceMapperProxyFactory = new MapperProxyFactory<>(
                OkInterface.class
        );
        assertEquals(interfaceMapperProxyFactory.getMapperInterface(), OkInterface.class);
        OkInterface okInterface = interfaceMapperProxyFactory.newInstance(mockReactiveSqlSession);
        R2dbcMapperAnnotationBuilder parser = new R2dbcMapperAnnotationBuilder(r2dbcMybatisConfiguration,
                OkInterface.class
        );
        parser.parse();
        okInterface.returnMonoWithInteger()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        okInterface.returnMonoWithVoid()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        okInterface.returnMonoWithLong()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        okInterface.returnMonoWithObject()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        okInterface.returnMonoWithBoolean()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        okInterface.returnFluxWithObject()
                .as(StepVerifier::create)
                .expectError(Throwable.class)
                .verify();
        Map<Method, MapperProxy.MapperMethodInvoker> methodCache = interfaceMapperProxyFactory.getMethodCache();
        assertEquals(methodCache.size(), 6);
    }

    @Mapper
    public interface OkInterface {

        @Delete("select 1")
        Mono<Integer> returnMonoWithInteger();

        @Update("select 1")
        Mono<Void> returnMonoWithVoid();

        @Insert("insert 1")
        Mono<Long> returnMonoWithLong();

        @Select("select 1")
        Mono<Object> returnMonoWithObject();

        @Select("select 1")
        Mono<Boolean> returnMonoWithBoolean();

        @Select("select 1")
        Flux<Object> returnFluxWithObject();

    }

    public interface ErrorInterface {

        Integer returnWithInteger();

        void returnWithVoid();

        List<Object> returnWithListObject();
    }

    public interface BindingInterface {

    }

    public static class BindingClass {

    }

}

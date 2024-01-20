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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.builder;

import org.apache.ibatis.builder.annotation.MethodResolver;

import java.lang.reflect.Method;

/**
 * The type R2dbc mapper method resolver.
 *
 * @author Eduardo Macarron
 * @author Gang Cheng
 */
public class R2dbcMapperMethodResolver extends MethodResolver {

    private final R2dbcMapperAnnotationBuilder annotationBuilder;
    private final Method method;

    /**
     * Instantiates a new R2dbc mapper method resolver.
     *
     * @param annotationBuilder the annotation builder
     * @param method            the method
     */
    public R2dbcMapperMethodResolver(R2dbcMapperAnnotationBuilder annotationBuilder, Method method) {
        super(annotationBuilder, method);
        this.annotationBuilder = annotationBuilder;
        this.method = method;
    }


    @Override
    public void resolve() {
        annotationBuilder.parseStatement(method);
    }

}
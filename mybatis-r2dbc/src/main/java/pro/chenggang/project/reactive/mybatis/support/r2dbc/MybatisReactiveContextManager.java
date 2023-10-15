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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.R2dbcStatementLog;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support.ReactiveExecutorContextAttribute;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * The interface Mybatis reactive context helper.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface MybatisReactiveContextManager {

    /**
     * read current context.
     *
     * @return mono mono
     */
    static Mono<ReactiveExecutorContext> currentContext() {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveExecutorContext is empty")))
                .cast(ReactiveExecutorContext.class)
        );
    }

    /**
     * read current context attribute.
     *
     * @return mono mono
     */
    static Mono<ReactiveExecutorContextAttribute> currentContextAttribute() {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContextAttribute.class))
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveExecutorContextAttribute is empty")))
                .cast(ReactiveExecutorContextAttribute.class)
        );
    }

    /**
     * Init reactive executor context attribute.
     *
     * @param context           the context
     * @param attributeConsumer the attribute consumer
     * @return the context
     */
    static Context initReactiveExecutorContextAttribute(Context context, Consumer<ReactiveExecutorContextAttribute> attributeConsumer){
        Optional<ReactiveExecutorContextAttribute> optionalContext = context.getOrEmpty(ReactiveExecutorContextAttribute.class)
                .map(ReactiveExecutorContextAttribute.class::cast);
        if(optionalContext.isPresent()){
            optionalContext.ifPresent(attributeConsumer);
            return context;
        }
        ReactiveExecutorContextAttribute reactiveExecutorContextAttribute = new ReactiveExecutorContextAttribute();
        attributeConsumer.accept(reactiveExecutorContextAttribute);
        return context.put(ReactiveExecutorContextAttribute.class, reactiveExecutorContextAttribute);
    }

    /**
     * Init reactive executor context attribute.
     *
     * @param context the context
     * @return the context
     */
    static Context initReactiveExecutorContextAttribute(Context context){
        return initReactiveExecutorContextAttribute(context,attribute -> {});
    }

    /**
     * init reactive executor context with R2dbcStatementLog
     *
     * @param context           the context
     * @param r2dbcStatementLog the statement log helper
     * @return context context
     */
    Context initReactiveExecutorContext(Context context, R2dbcStatementLog r2dbcStatementLog);

    /**
     * init reactive executor context
     *
     * @param context the context
     * @return context context
     */
    Context initReactiveExecutorContext(Context context);
}

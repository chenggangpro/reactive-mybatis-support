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

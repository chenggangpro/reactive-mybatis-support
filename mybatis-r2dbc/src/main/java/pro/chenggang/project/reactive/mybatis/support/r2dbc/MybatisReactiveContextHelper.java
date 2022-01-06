package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.ReactiveExecutorContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.StatementLogHelper;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * The interface Mybatis reactive context helper.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12 /16/21.
 */
public interface MybatisReactiveContextHelper {

    /**
     * current context
     *
     * @return mono
     */
    static Mono<ReactiveExecutorContext> currentContext() {
        return Mono.deferContextual(contextView -> Mono
                .justOrEmpty(contextView.getOrEmpty(ReactiveExecutorContext.class))
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveExecutorContext is empty")))
                .cast(ReactiveExecutorContext.class)
        );
    }

    /**
     * init reactive executor context with StatementLogHelper
     *
     * @param context            the context
     * @param statementLogHelper the statement log helper
     * @return context
     */
    Context initReactiveExecutorContext(Context context, StatementLogHelper statementLogHelper);

    /**
     * init reactive executor context
     *
     * @param context the context
     * @return context
     */
    Context initReactiveExecutorContext(Context context);
}

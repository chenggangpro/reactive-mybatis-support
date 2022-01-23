package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.handler;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;

import java.util.List;

/**
 * The interface Reactive result handler.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public interface ReactiveResultHandler {

    /**
     * deferred object
     */
    Object DEFERRED = new Object();

    /**
     * get result row total count
     *
     * @return result row total count
     */
    Integer getResultRowTotalCount();

    /**
     * handle result
     *
     * @param <T>              the type parameter
     * @param rowResultWrapper the row result wrapper
     * @return list
     */
    <T> List<T> handleResult(RowResultWrapper rowResultWrapper);
}

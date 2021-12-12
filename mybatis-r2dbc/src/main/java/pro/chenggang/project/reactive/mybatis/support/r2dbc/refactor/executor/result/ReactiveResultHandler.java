package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result;

import org.reactivestreams.Publisher;

/**
 * @author: chenggang
 * @date 12/10/21.
 */
public interface ReactiveResultHandler {

    /**
     * deferred object
     */
    Object DEFERRED = new Object();

    /**
     * get result row total count
     * @return
     */
    Integer getResultRowTotalCount();

    /**
     * handle result
     * @param rowResultWrapper
     * @param <T>
     * @return
     */
    <T> Publisher<T> handleResult(RowResultWrapper rowResultWrapper);
}

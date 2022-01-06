package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.key;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.result.RowResultWrapper;

/**
 * The interface R2dbc key generator.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12 /12/21.
 */
public interface R2dbcKeyGenerator {

    /**
     * get result row count
     *
     * @return result row count
     */
    Integer getResultRowCount();

    /**
     * handle key result
     *
     * @param rowResultWrapper the row result wrapper
     * @param parameter        the parameter
     * @return integer
     */
    Integer handleKeyResult(RowResultWrapper rowResultWrapper, Object parameter);
}

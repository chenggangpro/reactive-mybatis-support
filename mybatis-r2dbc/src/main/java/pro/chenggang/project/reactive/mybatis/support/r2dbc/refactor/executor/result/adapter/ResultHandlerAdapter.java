package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public interface ResultHandlerAdapter<T> {

    /**
     * adapted class
     * @return
     */
    Class<T> adaptClazz();

    /**
     * get result by columnName
     * @param row
     * @param rowMetadata
     * @param columnName
     * @return
     */
    T getResult(Row row, RowMetadata rowMetadata,String columnName);

    /**
     * get result by columnIndex
     * @param row
     * @param rowMetadata
     * @param columnIndex
     * @return
     */
    T getResult(Row row, RowMetadata rowMetadata,int columnIndex);
}

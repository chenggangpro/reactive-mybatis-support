package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;

/**
 * The interface R2dbc type handler adapter.
 *
 * @param <T> the type parameter
 * @author Gang Cheng
 * @version 1.0.0
 * @date 12/9/21.
 */
public interface R2dbcTypeHandlerAdapter<T> {

    /**
     * adapted class
     *
     * @return class
     */
    Class<T> adaptClazz();

    /**
     * setParameter
     *
     * @param statement               the statement
     * @param parameterHandlerContext the parameter handler context
     * @param parameter               the parameter
     */
    void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, T parameter);

    /**
     * get result by columnName
     *
     * @param row         the row
     * @param rowMetadata the row metadata
     * @param columnName  the column name
     * @return result
     */
    T getResult(Row row, RowMetadata rowMetadata, String columnName);

    /**
     * get result by columnIndex
     *
     * @param row         the row
     * @param rowMetadata the row metadata
     * @param columnIndex the column index
     * @return result
     */
    T getResult(Row row, RowMetadata rowMetadata, int columnIndex);
}

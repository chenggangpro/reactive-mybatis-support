package pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.type;

import io.r2dbc.spi.R2dbcException;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import org.apache.ibatis.type.JdbcType;

/**
 * R2DBC Type handler
 * copy from https://github.com/linux-china/mybatis-r2dbc
 * @author linux_china
 */
public interface R2DBCTypeHandler<T> {

    /**
     * set parameters
     * @param statement
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws R2dbcException
     */
    void setParameter(Statement statement, int i, T parameter, JdbcType jdbcType) throws R2dbcException;

    /**
     * get result
     * @param row
     * @param columnName
     * @param rowMetadata
     * @return
     * @throws R2dbcException
     */
    T getResult(Row row, String columnName, RowMetadata rowMetadata) throws R2dbcException;

    /**
     * get result
     * @param row
     * @param columnIndex
     * @param rowMetadata
     * @return
     * @throws R2dbcException
     */
    T getResult(Row row, int columnIndex, RowMetadata rowMetadata) throws R2dbcException;

    /**
     * get current support type
     * @return
     */
    Class<?> getType();
}

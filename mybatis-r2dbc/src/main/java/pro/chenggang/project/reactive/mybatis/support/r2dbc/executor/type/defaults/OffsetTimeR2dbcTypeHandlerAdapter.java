package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * The type Offset time r2dbc type handler adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @date 12 /9/21.
 */
public class OffsetTimeR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<OffsetTime> {

    @Override
    public Class<OffsetTime> adaptClazz() {
        return OffsetTime.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, OffsetTime parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.toLocalTime());
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalTime localTime = row.get(columnName, LocalTime.class);
        if (null == localTime) {
            return null;
        }
        return OffsetTime.from(localTime);
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalTime localTime = row.get(columnIndex, LocalTime.class);
        if (null == localTime) {
            return null;
        }
        return OffsetTime.from(localTime);
    }

}

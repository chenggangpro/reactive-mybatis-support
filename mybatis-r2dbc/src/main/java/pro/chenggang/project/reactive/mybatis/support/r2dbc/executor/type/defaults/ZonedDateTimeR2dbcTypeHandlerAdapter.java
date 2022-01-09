package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * The type Zoned date time r2dbc type handler adapter.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @date 12 /9/21.
 */
public class ZonedDateTimeR2dbcTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> adaptClazz() {
        return ZonedDateTime.class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, ZonedDateTime parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.toOffsetDateTime());
    }

    @Override
    public ZonedDateTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        OffsetDateTime offsetDateTime = row.get(columnName, OffsetDateTime.class);
        if (null == offsetDateTime) {
            return null;
        }
        return offsetDateTime.toZonedDateTime();
    }

    @Override
    public ZonedDateTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        OffsetDateTime offsetDateTime = row.get(columnIndex, OffsetDateTime.class);
        if (null == offsetDateTime) {
            return null;
        }
        return offsetDateTime.toZonedDateTime();
    }

}

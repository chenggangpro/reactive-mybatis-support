package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ZonedDateTimeResultHandlerAdapter implements ResultHandlerAdapter<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> adaptClazz() {
        return ZonedDateTime.class;
    }

    @Override
    public ZonedDateTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        OffsetDateTime offsetDateTime = row.get(columnName, OffsetDateTime.class);
        if(null == offsetDateTime){
            return null;
        }
        return offsetDateTime.toZonedDateTime();
    }

    @Override
    public ZonedDateTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        OffsetDateTime offsetDateTime = row.get(columnIndex, OffsetDateTime.class);
        if(null == offsetDateTime){
            return null;
        }
        return offsetDateTime.toZonedDateTime();
    }

}

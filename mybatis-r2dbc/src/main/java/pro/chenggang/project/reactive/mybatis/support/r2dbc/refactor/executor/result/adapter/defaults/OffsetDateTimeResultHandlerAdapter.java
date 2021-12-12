package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class OffsetDateTimeResultHandlerAdapter implements ResultHandlerAdapter<OffsetDateTime> {

    @Override
    public Class<OffsetDateTime> adaptClazz() {
        return OffsetDateTime.class;
    }

    @Override
    public OffsetDateTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalDateTime localDateTime = row.get(columnName, LocalDateTime.class);
        if(null == localDateTime){
            return null;
        }
        return OffsetDateTime.from(localDateTime);
    }

    @Override
    public OffsetDateTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalDateTime localDateTime = row.get(columnIndex, LocalDateTime.class);
        if(null == localDateTime){
            return null;
        }
        return OffsetDateTime.from(localDateTime);
    }

}

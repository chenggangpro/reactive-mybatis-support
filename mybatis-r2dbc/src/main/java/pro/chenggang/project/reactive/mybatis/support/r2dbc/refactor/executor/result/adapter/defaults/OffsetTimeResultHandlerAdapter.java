package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class OffsetTimeResultHandlerAdapter implements ResultHandlerAdapter<OffsetTime> {

    @Override
    public Class<OffsetTime> adaptClazz() {
        return OffsetTime.class;
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalTime localTime = row.get(columnName, LocalTime.class);
        if(null == localTime){
            return null;
        }
        return OffsetTime.from(localTime);
    }

    @Override
    public OffsetTime getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalTime localTime = row.get(columnIndex, LocalTime.class);
        if(null == localTime){
            return null;
        }
        return OffsetTime.from(localTime);
    }

}

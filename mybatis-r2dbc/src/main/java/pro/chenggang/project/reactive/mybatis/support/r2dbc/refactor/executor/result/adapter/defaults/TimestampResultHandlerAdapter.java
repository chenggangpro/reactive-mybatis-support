package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class TimestampResultHandlerAdapter implements ResultHandlerAdapter<Timestamp> {

    @Override
    public Class<Timestamp> adaptClazz() {
        return Timestamp.class;
    }

    @Override
    public Timestamp getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalDateTime localDateTime = row.get(columnName, LocalDateTime.class);
        if(null == localDateTime){
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    @Override
    public Timestamp getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalDateTime localDateTime = row.get(columnIndex, LocalDateTime.class);
        if(null == localDateTime){
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

}

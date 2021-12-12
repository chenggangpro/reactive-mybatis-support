package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.sql.Time;
import java.time.LocalTime;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class SqlTimeResultHandlerAdapter implements ResultHandlerAdapter<Time> {

    @Override
    public Class<Time> adaptClazz() {
        return Time.class;
    }

    @Override
    public Time getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalTime localTime = row.get(columnName, LocalTime.class);
        if(null == localTime){
            return null;
        }
        return Time.valueOf(localTime);
    }

    @Override
    public Time getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalTime localTime = row.get(columnIndex, LocalTime.class);
        if(null == localTime){
            return null;
        }
        return Time.valueOf(localTime);
    }

}

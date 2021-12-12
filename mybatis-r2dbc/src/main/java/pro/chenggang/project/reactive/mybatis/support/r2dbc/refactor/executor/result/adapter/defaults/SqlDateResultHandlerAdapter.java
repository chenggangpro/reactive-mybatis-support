package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.sql.Date;
import java.time.LocalDate;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class SqlDateResultHandlerAdapter implements ResultHandlerAdapter<Date> {

    @Override
    public Class<Date> adaptClazz() {
        return Date.class;
    }

    @Override
    public Date getResult(Row row, RowMetadata rowMetadata, String columnName) {
        LocalDate localDate = row.get(columnName, LocalDate.class);
        if(null == localDate){
            return null;
        }
        return Date.valueOf(localDate);
    }

    @Override
    public Date getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        LocalDate localDate = row.get(columnIndex, LocalDate.class);
        if(null == localDate){
            return null;
        }
        return Date.valueOf(localDate);
    }

}

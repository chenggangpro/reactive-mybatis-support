package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.nio.ByteBuffer;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ByteArrayResultHandlerAdapter implements ResultHandlerAdapter<byte[]> {

    @Override
    public Class<byte[]> adaptClazz() {
        return byte[].class;
    }

    @Override
    public byte[] getResult(Row row, RowMetadata rowMetadata, String columnName) {
        ByteBuffer byteBuffer = row.get(columnName, ByteBuffer.class);
        if(null == byteBuffer){
            return null;
        }
        return byteBuffer.array();
    }

    @Override
    public byte[] getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        ByteBuffer byteBuffer = row.get(columnIndex, ByteBuffer.class);
        if(null == byteBuffer){
            return null;
        }
        return byteBuffer.array();
    }

}

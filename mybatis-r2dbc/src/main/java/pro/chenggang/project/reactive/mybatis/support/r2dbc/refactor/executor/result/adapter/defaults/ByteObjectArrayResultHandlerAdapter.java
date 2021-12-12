package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.defaults;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.result.adapter.ResultHandlerAdapter;

import java.nio.ByteBuffer;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ByteObjectArrayResultHandlerAdapter implements ResultHandlerAdapter<Byte[]> {

    @Override
    public Class<Byte[]> adaptClazz() {
        return Byte[].class;
    }

    @Override
    public Byte[] getResult(Row row, RowMetadata rowMetadata, String columnName) {
        ByteBuffer byteBuffer = row.get(columnName, ByteBuffer.class);
        if(null == byteBuffer){
            return null;
        }
        return this.toByteObjects(byteBuffer.array());
    }

    @Override
    public Byte[] getResult(Row row, RowMetadata rowMetadata, int columnIndex) {
        ByteBuffer byteBuffer = row.get(columnIndex, ByteBuffer.class);
        if(null == byteBuffer){
            return null;
        }
        return this.toByteObjects(byteBuffer.array());
    }


    /**
     * byte[] -> Byte[]
     * @param oBytes
     * @return
     */
    private Byte[] toByteObjects(byte[] oBytes) {
        Byte[] bytes = new Byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }
}

package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.nio.ByteBuffer;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ByteObjectArrayParameterAdapter implements JdbcParameterAdapter<Byte[]> {

    @Override
    public Class<Byte[]> adaptClazz() {
        return Byte[].class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, Byte[] parameter) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.toPrimitives(parameter));
        statement.bind(parameterHandlerContext.getIndex(),byteBuffer);
    }

    /**
     * Byte[] -> byte[]
     * @param oBytes
     * @return
     */
    private byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }
}

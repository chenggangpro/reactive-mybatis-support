package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.JdbcParameterAdapter;

import java.nio.ByteBuffer;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ByteArrayParameterAdapter implements JdbcParameterAdapter<byte[]> {

    @Override
    public Class<byte[]> adaptClazz() {
        return byte[].class;
    }

    @Override
    public void adapt(Statement statement, ParameterHandlerContext parameterHandlerContext, byte[] parameter) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(parameter);
        statement.bind(parameterHandlerContext.getIndex(),byteBuffer);
    }

}

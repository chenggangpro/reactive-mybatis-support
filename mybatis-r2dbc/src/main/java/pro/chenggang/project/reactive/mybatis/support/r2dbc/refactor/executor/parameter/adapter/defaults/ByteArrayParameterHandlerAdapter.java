package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.defaults;

import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.executor.parameter.adapter.ParameterHandlerAdapter;

import java.nio.ByteBuffer;

/**
 * @author: chenggang
 * @date 12/9/21.
 */
public class ByteArrayParameterHandlerAdapter implements ParameterHandlerAdapter<byte[]> {

    @Override
    public Class<byte[]> adaptClazz() {
        return byte[].class;
    }

    @Override
    public void setParameter(Statement statement, ParameterHandlerContext parameterHandlerContext, byte[] parameter) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(parameter);
        statement.bind(parameterHandlerContext.getIndex(),byteBuffer);
    }

}

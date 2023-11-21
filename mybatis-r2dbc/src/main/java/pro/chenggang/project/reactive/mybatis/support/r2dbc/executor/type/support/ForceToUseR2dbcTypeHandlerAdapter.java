package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.support;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Gang Cheng
 * @version 1.0.0
 * @since 1.0.0
 */
public class ForceToUseR2dbcTypeHandlerAdapter extends BaseTypeHandler<Object> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps,
                                    int i,
                                    Object parameter,
                                    JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("Use r2dbc type handler adapter (TypeHandler) should not doing anything"); 
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        throw new UnsupportedOperationException("Use r2dbc type handler adapter (TypeHandler) should not doing anything");
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Use r2dbc type handler adapter (TypeHandler) should not doing anything");
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Use r2dbc type handler adapter (TypeHandler) should not doing anything");
    }
}

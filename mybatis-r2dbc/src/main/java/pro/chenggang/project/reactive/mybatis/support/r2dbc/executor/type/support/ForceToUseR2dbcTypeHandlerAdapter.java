/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

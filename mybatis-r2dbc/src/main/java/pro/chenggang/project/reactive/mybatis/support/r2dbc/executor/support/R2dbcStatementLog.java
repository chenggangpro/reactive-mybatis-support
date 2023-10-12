package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ArrayUtil;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Statement log helper. modify according to ConnectionLogger
 * <p>
 * {@link org.apache.ibatis.logging.jdbc.ConnectionLogger}
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcStatementLog {

    private final Log statementLog;

    /**
     * Instantiates a new Statement log helper.
     *
     * @param statementLog the statement log
     */
    public R2dbcStatementLog(Log statementLog) {
        this.statementLog = statementLog;
    }

    /**
     * Log sql.
     *
     * @param sql the sql
     */
    public void logSql(String sql) {
        if (statementLog.isDebugEnabled()) {
            debug(" Preparing: " + removeExtraWhitespace(sql), true);
        }
    }

    /**
     * Log parameters.
     *
     * @param columnValues the column values
     */
    public void logParameters(List<Object> columnValues) {
        debug("Parameters: " + getParameterValueString(columnValues), true);
    }

    /**
     * Log updates.
     *
     * @param updateCount the update count
     */
    public void logUpdates(Long updateCount) {
        debug("   Updates: " + updateCount, false);
    }

    /**
     * Log total.
     *
     * @param rows the rows
     */
    public void logTotal(Integer rows) {
        debug("     Total: " + rows, false);
    }

    private String getParameterValueString(List<Object> columnValues) {
        List<Object> typeList = new ArrayList<>(columnValues.size());
        for (Object value : columnValues) {
            if (value == null) {
                typeList.add("null");
            } else {
                typeList.add(objectValueString(value) + "(" + value.getClass().getSimpleName() + ")");
            }
        }
        final String parameters = typeList.toString();
        return parameters.substring(1, parameters.length() - 1);
    }

    private String objectValueString(Object value) {
        if (value instanceof Array) {
            try {
                return ArrayUtil.toString(((Array) value).getArray());
            } catch (SQLException e) {
                return value.toString();
            }
        }
        return value.toString();
    }

    private String removeExtraWhitespace(String original) {
        return SqlSourceBuilder.removeExtraWhitespaces(original);
    }

    private void debug(String text, boolean input) {
        if (statementLog.isDebugEnabled()) {
            statementLog.debug(prefix(input) + text);
        }
    }

    private void trace(String text, boolean input) {
        if (statementLog.isTraceEnabled()) {
            statementLog.trace(prefix(input) + text);
        }
    }

    private String prefix(boolean isInput) {
        char[] buffer = new char[2 + 2];
        Arrays.fill(buffer, '=');
        buffer[2 + 1] = ' ';
        if (isInput) {
            buffer[2] = '>';
        } else {
            buffer[0] = '<';
        }
        return new String(buffer);
    }
}
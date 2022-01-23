package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter;

import org.apache.ibatis.type.JdbcType;

/**
 * The type Parameter handler context.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class ParameterHandlerContext {

    private int index;
    private JdbcType jdbcType;
    private Class<?> javaType;


    /**
     * Gets index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets jdbc type.
     *
     * @return the jdbc type
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }

    /**
     * Sets jdbc type.
     *
     * @param jdbcType the jdbc type
     */
    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
    }

    /**
     * Gets java type.
     *
     * @return the java type
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Sets java type.
     *
     * @param javaType the java type
     */
    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }
}

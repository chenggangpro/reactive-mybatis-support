package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;
import java.time.LocalDateTime;

public final class DeptDynamicSqlSupport {
    public static final Dept dept = new Dept();

    public static final SqlColumn<Long> deptNo = dept.deptNo;

    public static final SqlColumn<String> deptName = dept.deptName;

    public static final SqlColumn<String> location = dept.location;

    public static final SqlColumn<LocalDateTime> createTime = dept.createTime;

    public static final class Dept extends SqlTable {
        public final SqlColumn<Long> deptNo = column("dept_no", JDBCType.BIGINT);

        public final SqlColumn<String> deptName = column("dept_name", JDBCType.VARCHAR);

        public final SqlColumn<String> location = column("location", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> createTime = column("create_time", JDBCType.TIMESTAMP);

        public Dept() {
            super("dept");
        }
    }
}
package pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic;

import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

import java.sql.JDBCType;
import java.time.LocalDate;

/**
 * auto generated dynamic mapper
 * 
 * @author autoGenerated
 */
public final class ProjectDynamicSqlSupport {
    public static final Project project = new Project();

    /**
     * project no
     */
    public static final SqlColumn<Long> projectId = project.projectId;

    /**
     * emp no
     */
    public static final SqlColumn<Long> empNo = project.empNo;

    /**
     * start date
     */
    public static final SqlColumn<LocalDate> startDate = project.startDate;

    /**
     * end date
     */
    public static final SqlColumn<LocalDate> endDate = project.endDate;

    public static final class Project extends AliasableSqlTable<Project> {
        public final SqlColumn<Long> projectId = column("project_id", JDBCType.BIGINT);

        public final SqlColumn<Long> empNo = column("emp_no", JDBCType.BIGINT);

        public final SqlColumn<LocalDate> startDate = column("start_date", JDBCType.DATE);

        public final SqlColumn<LocalDate> endDate = column("end_date", JDBCType.DATE);

        public Project() {
            super("project", Project::new);
        }
    }
}
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
package pro.chenggang.project.reactive.mybatis.support.common.mapper.dynamic;

import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * emp dynamic mapper
 * 
 * @author autoGenerated
 */
public final class EmpDynamicSqlSupport {
    public static final Emp emp = new Emp();

    /**
     * emp no
     */
    public static final SqlColumn<Long> empNo = emp.empNo;

    /**
     * emp name
     */
    public static final SqlColumn<String> empName = emp.empName;

    /**
     * job
     */
    public static final SqlColumn<String> job = emp.job;

    /**
     * manager
     */
    public static final SqlColumn<String> manager = emp.manager;

    /**
     * hire date
     */
    public static final SqlColumn<LocalDate> hireDate = emp.hireDate;

    /**
     * salary
     */
    public static final SqlColumn<Integer> salary = emp.salary;

    /**
     * kpi
     */
    public static final SqlColumn<BigDecimal> kpi = emp.kpi;

    /**
     * dept no
     */
    public static final SqlColumn<Long> deptNo = emp.deptNo;

    /**
     * create time
     */
    public static final SqlColumn<LocalDateTime> createTime = emp.createTime;

    public static final class Emp extends AliasableSqlTable<Emp> {
        public final SqlColumn<Long> empNo = column("emp_no", JDBCType.BIGINT);

        public final SqlColumn<String> empName = column("emp_name", JDBCType.VARCHAR);

        public final SqlColumn<String> job = column("job", JDBCType.VARCHAR);

        public final SqlColumn<String> manager = column("manager", JDBCType.VARCHAR);

        public final SqlColumn<LocalDate> hireDate = column("hire_date", JDBCType.DATE);

        public final SqlColumn<Integer> salary = column("salary", JDBCType.INTEGER);

        public final SqlColumn<BigDecimal> kpi = column("kpi", JDBCType.DECIMAL);

        public final SqlColumn<Long> deptNo = column("dept_no", JDBCType.BIGINT);

        public final SqlColumn<LocalDateTime> createTime = column("create_time", JDBCType.TIMESTAMP);

        public Emp() {
            super("emp", Emp::new);
        }
    }
}
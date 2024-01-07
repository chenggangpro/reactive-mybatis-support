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
package pro.chenggang.project.reactive.mybatis.support.generator.mapper.dynamic;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EmpDynamicSqlSupport {
    public static final Emp emp = new Emp();

    public static final SqlColumn<Long> empNo = emp.empNo;

    public static final SqlColumn<String> empName = emp.empName;

    public static final SqlColumn<String> job = emp.job;

    public static final SqlColumn<String> manager = emp.manager;

    public static final SqlColumn<LocalDate> hireDate = emp.hireDate;

    public static final SqlColumn<Integer> salary = emp.salary;

    public static final SqlColumn<BigDecimal> kpi = emp.kpi;

    public static final SqlColumn<Long> deptNo = emp.deptNo;

    public static final SqlColumn<LocalDateTime> createTime = emp.createTime;

    public static final class Emp extends SqlTable {
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
            super("emp");
        }
    }
}
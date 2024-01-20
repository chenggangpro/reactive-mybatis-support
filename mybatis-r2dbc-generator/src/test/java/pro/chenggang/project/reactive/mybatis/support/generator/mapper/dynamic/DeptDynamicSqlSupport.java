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
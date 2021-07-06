package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.application.mapper.dynamic;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;
import java.time.LocalDateTime;

public final class PeopleDynamicSqlSupport {
    public static final People people = new People();

    public static final SqlColumn<Integer> id = people.id;

    public static final SqlColumn<String> nick = people.nick;

    public static final SqlColumn<LocalDateTime> createdAt = people.createdAt;

    public static final class People extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> nick = column("nick", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public People() {
            super("people");
        }
    }
}
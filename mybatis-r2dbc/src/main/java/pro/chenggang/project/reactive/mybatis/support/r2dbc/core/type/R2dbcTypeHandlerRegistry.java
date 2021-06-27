package pro.chenggang.project.reactive.mybatis.support.r2dbc.core.type;

import org.apache.ibatis.type.MappedTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Type handler registry
 * copy from https://github.com/linux-china/mybatis-r2dbc
 * @author linux_china
 */
public class R2dbcTypeHandlerRegistry {

    private final Map<Class<?>, R2DBCTypeHandler<?>> allTypeHandlersMap = new HashMap<>();

    public R2dbcTypeHandlerRegistry() {
        register(java.util.Date.class, new R2dbcDateTypeHandler());
        register(java.sql.Date.class, new R2dbcSqlDateTypeHandler());
        register(java.sql.Time.class, new R2dbcSqlTimeTypeHandler());
        register(java.sql.Timestamp.class, new R2dbcSqlTimestampTypeHandler());
    }

    public boolean hasTypeHandler(Class<?> javaType) {
        return allTypeHandlersMap.containsKey(javaType);
    }

    @SuppressWarnings("rawtypes")
    public R2DBCTypeHandler getTypeHandler(Class<?> javaType) {
        return allTypeHandlersMap.get(javaType);
    }

    public <T> void register(Class<T> javaType, R2DBCTypeHandler<? extends T> typeHandler) {
        allTypeHandlersMap.put(javaType, typeHandler);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(R2DBCTypeHandler<T> typeHandler) {
        MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
        if (mappedTypes != null) {
            for (Class handledType : mappedTypes.value()) {
                register(handledType, typeHandler);
            }
        }
        Class handledType = typeHandler.getType();
        if (handledType != null) {
            register(handledType, typeHandler);
        }
    }

}

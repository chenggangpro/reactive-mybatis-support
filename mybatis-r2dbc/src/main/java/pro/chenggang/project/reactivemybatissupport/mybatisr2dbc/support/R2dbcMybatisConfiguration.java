package pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.support;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.beans.factory.InitializingBean;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.ReactiveSqlSession;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.type.R2DBCTypeHandler;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.type.R2dbcTypeHandlerRegistry;

/**
 * @author: chenggang
 * @date 6/25/21.
 */
public class R2dbcMybatisConfiguration extends Configuration implements InitializingBean {

    private final R2dbcMapperRegistry r2dbcMapperRegistry = new R2dbcMapperRegistry(this);

    private final R2dbcTypeHandlerRegistry r2dbcTypeHandlerRegistry = new R2dbcTypeHandlerRegistry();

    @Getter
    @Setter
    private boolean enableMetrics;

    public <T> T getMapper(Class<T> type, ReactiveSqlSession session) {
        return this.r2dbcMapperRegistry.getMapper(type, session);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        this.r2dbcMapperRegistry.addMapper(type);
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        this.r2dbcMapperRegistry.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        this.r2dbcMapperRegistry.addMappers(packageName);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return this.r2dbcMapperRegistry.hasMapper(type);
    }

    public R2dbcTypeHandlerRegistry getR2dbcTypeHandlerRegistry() {
        return r2dbcTypeHandlerRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (TypeHandler<?> typeHandler : super.getTypeHandlerRegistry().getTypeHandlers()) {
            if (typeHandler instanceof R2DBCTypeHandler) {
                R2DBCTypeHandler<?> r2dbcTypeHandler = (R2DBCTypeHandler<?>) typeHandler;
                this.r2dbcTypeHandlerRegistry.register(r2dbcTypeHandler);
            }
        }
    }
}

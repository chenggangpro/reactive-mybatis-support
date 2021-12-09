package pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.delegate;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.binding.MapperProxyFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.refactor.builder.R2dbcMapperAnnotationBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenggang
 * @date 12/8/21.
 */
public class R2dbcMapperRegistry extends MapperRegistry {

    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
    private final R2dbcConfiguration configuration;

    public R2dbcMapperRegistry(R2dbcConfiguration config) {
        super(config);
        this.configuration = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, ReactiveSqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    @Override
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                knownMappers.put(type, new MapperProxyFactory<>(type));
                R2dbcMapperAnnotationBuilder parser = new R2dbcMapperAnnotationBuilder(configuration, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    @Override
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }
}

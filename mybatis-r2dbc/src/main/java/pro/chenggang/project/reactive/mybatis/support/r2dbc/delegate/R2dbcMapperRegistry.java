package pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSession;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.binding.MapperProxyFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.builder.R2dbcMapperAnnotationBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The type R2dbc mapper registry.
 *
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcMapperRegistry extends MapperRegistry {

    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
    private final R2dbcMybatisConfiguration configuration;

    /**
     * Instantiates a new R2dbc mapper registry.
     *
     * @param r2dbcMybatisConfiguration the r2dbcMybatisConfiguration
     */
    public R2dbcMapperRegistry(R2dbcMybatisConfiguration r2dbcMybatisConfiguration) {
        super(r2dbcMybatisConfiguration);
        this.configuration = r2dbcMybatisConfiguration;
    }

    /**
     * Gets mapper.
     *
     * @param <T>                the type parameter
     * @param type               the type
     * @param reactiveSqlSession the reactive sql session
     * @return the mapper
     */
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, ReactiveSqlSession reactiveSqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(reactiveSqlSession);
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

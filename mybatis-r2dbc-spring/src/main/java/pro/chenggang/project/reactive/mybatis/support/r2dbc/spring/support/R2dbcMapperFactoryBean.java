package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support;

import org.apache.ibatis.executor.ErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;

import static org.springframework.util.Assert.notNull;

/**
 * R2dbcMapperFactoryBean
 *
 * @param <T> the type parameter
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcMapperFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(R2dbcMapperFactoryBean.class);

    private Class<T> mapperInterface;

    private ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    /**
     * Instantiates a new r2dbc mapper factory bean.
     */
    public R2dbcMapperFactoryBean() {

    }

    /**
     * Instantiates a new r2dbc mapper factory bean.
     *
     * @param clazz the clazz
     */
    public R2dbcMapperFactoryBean(Class<T> clazz) {
        this.mapperInterface = clazz;
    }

    @Override
    public T getObject() throws Exception {
        return reactiveSqlSessionFactory.openSession()
                .getMapper(this.mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Sets mapper interface.
     *
     * @param mapperInterface the mapper interface
     */
    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * Sets sql session factory.
     *
     * @param reactiveSqlSessionFactory the reactive sql session factory
     */
    public void setSqlSessionFactory(ReactiveSqlSessionFactory reactiveSqlSessionFactory) {
        this.reactiveSqlSessionFactory = reactiveSqlSessionFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.reactiveSqlSessionFactory, "Property 'sqlSessionFactory' are required...");
        if (!reactiveSqlSessionFactory.getConfiguration().hasMapper(this.mapperInterface)) {
            try {
                reactiveSqlSessionFactory.getConfiguration().addMapper(this.mapperInterface);
            } catch (Exception e) {
                log.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }
}

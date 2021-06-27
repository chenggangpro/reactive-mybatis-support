package pro.chenggang.project.reactivemybatissupport.mybatisr2dbcspring.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.ErrorContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.ReactiveSqlSession;
import pro.chenggang.project.reactivemybatissupport.mybatisr2dbc.core.ReactiveSqlSessionFactory;

import static org.springframework.util.Assert.notNull;

/**
 * R2dbcMapperFactoryBean
 * @param <T>
 * @author evans
 */
@Slf4j
public class R2dbcMapperFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private Class<T> mapperInterface;

    private ReactiveSqlSession sqlSession;

    public R2dbcMapperFactoryBean() {

    }

    public R2dbcMapperFactoryBean(Class<T> clazz) {
        this.mapperInterface = clazz;
    }

    @Override
    public T getObject() throws Exception {
        return sqlSession.getMapper(this.mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public void setSqlSessionFactory(ReactiveSqlSessionFactory sqlSessionFactory) {
        this.sqlSession = sqlSessionFactory.openSession();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.sqlSession, "Property 'sqlSessionFactory' are required...");
        if (!sqlSession.getConfiguration().hasMapper(this.mapperInterface)) {
            try {
                sqlSession.getConfiguration().addMapper(this.mapperInterface);
            } catch (Exception e) {
                log.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }
}

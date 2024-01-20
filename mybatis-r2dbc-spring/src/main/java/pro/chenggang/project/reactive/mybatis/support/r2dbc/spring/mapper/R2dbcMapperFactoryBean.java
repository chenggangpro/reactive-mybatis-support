/*
 *    Copyright 2009-2024 the original author or authors.
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.mapper;

import org.apache.ibatis.executor.ErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.ReactiveSqlSessionFactory;

import static org.springframework.util.Assert.notNull;

/**
 * R2dbcMapperFactoryBean
 *
 * @param <T> the type parameter
 * @author Gang Cheng
 * @version 1.0.0
 */
public class R2dbcMapperFactoryBean<T> implements FactoryBean<T>, InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(R2dbcMapperFactoryBean.class);

    protected Class<T> mapperInterface;

    protected ReactiveSqlSessionFactory reactiveSqlSessionFactory;

    private boolean failFast;

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

    /**
     * If true, a final check is done on Configuration to assure that all mapped statements are fully loaded and there is
     * no one still pending to resolve includes. Defaults to false.
     *
     * @param failFast enable failFast
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.reactiveSqlSessionFactory, "Property 'reactiveSqlSessionFactory' are required...");
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (failFast) {
            // fail-fast -> check all statements are completed
            this.reactiveSqlSessionFactory.getConfiguration().getMappedStatementNames();
        }
    }
}

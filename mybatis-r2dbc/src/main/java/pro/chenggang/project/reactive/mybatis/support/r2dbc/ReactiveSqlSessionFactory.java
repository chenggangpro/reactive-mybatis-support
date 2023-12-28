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
package pro.chenggang.project.reactive.mybatis.support.r2dbc;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.delegate.R2dbcMybatisConfiguration;

/**
 * The interface Reactive sql session factory.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */
public interface ReactiveSqlSessionFactory extends AutoCloseable {

    /**
     * open session
     *
     * @param reactiveSqlSessionProfile the reactive sql session profile
     * @return reactive sql session
     */
    ReactiveSqlSession openSession(ReactiveSqlSessionProfile reactiveSqlSessionProfile);

    /**
     * Open session.
     *
     * @return the reactive sql session
     */
    default ReactiveSqlSession openSession() {
        return openSession(ReactiveSqlSession.DEFAULT_PROFILE);
    }

    /**
     * get R2dbcMybatisConfiguration
     *
     * @return configuration configuration
     */
    R2dbcMybatisConfiguration getConfiguration();
}

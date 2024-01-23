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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults;

import io.r2dbc.spi.IsolationLevel;

/**
 * The Reactive sql session profile.
 *
 * @author Gang Cheng
 * @version 1.0.0
 * @since 2.0.0
 */
public class ReactiveSqlSessionProfile {

    private final boolean autoCommit;
    private final IsolationLevel isolationLevel;
    private final boolean enableTransaction;
    private boolean forceToRollback;

    /**
     * Is auto commit.
     *
     * @return the boolean
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Gets isolation level.
     *
     * @return the isolation level
     */
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    /**
     * Is enable transaction.
     *
     * @return the boolean
     */
    public boolean isEnableTransaction() {
        return enableTransaction;
    }

    /**
     * Configure session force to rollback.
     */
    public void forceToRollback() {
        this.forceToRollback = true;
    }

    /**
     * Is force to rollback.
     *
     * @return the true or false
     */
    public boolean isForceToRollback() {
        return this.forceToRollback;
    }

    private ReactiveSqlSessionProfile(boolean autoCommit, IsolationLevel isolationLevel, boolean enableTransaction) {
        if (enableTransaction) {
            autoCommit = false;
        }
        this.autoCommit = autoCommit;
        this.isolationLevel = isolationLevel;
        this.enableTransaction = enableTransaction;
    }

    /**
     * New reactive sql session profile.
     *
     * @param autoCommit        the auto commit
     * @param isolationLevel    the isolation level
     * @param enableTransaction the enable transaction
     * @return the reactive sql session profile
     */
    public static ReactiveSqlSessionProfile of(boolean autoCommit,
                                               IsolationLevel isolationLevel,
                                               boolean enableTransaction) {
        return new ReactiveSqlSessionProfile(autoCommit, isolationLevel, enableTransaction);
    }

    /**
     * New reactive sql session profile without isolation level.
     *
     * @param autoCommit        the auto commit
     * @param enableTransaction the enable transaction
     * @return the reactive sql session profile
     */
    public static ReactiveSqlSessionProfile of(boolean autoCommit, boolean enableTransaction) {
        return new ReactiveSqlSessionProfile(autoCommit, null, enableTransaction);
    }

    /**
     * New reactive sql session profile with isolation level
     *
     * @param isolationLevel the isolation level
     * @return the reactive sql session profile
     */
    public static ReactiveSqlSessionProfile of(IsolationLevel isolationLevel) {
        return new ReactiveSqlSessionProfile(false, isolationLevel, true);
    }
}
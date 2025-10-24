/*
 *    Copyright 2009-2025 the original author or authors.
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

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration profile for reactive SQL sessions that encapsulates transaction and connection settings.
 * This class provides an immutable configuration object that defines how reactive SQL sessions should behave,
 * including transaction management, isolation levels, timeouts, and auto-commit behavior.
 *
 * @author Gang Cheng
 * @version 2.0.0
 * @since 2.0.0
 */
public class ReactiveSqlSessionProfile {

    /**
     * Default profile instance with standard configuration settings.
     * This profile is created using the builder with default values:
     * <li>auto-commit enabled</li>
     * <li>no isolation level specified</li>
     * <li>transaction mode disabled</li>
     * <li>no forced rollback</li>
     * <li>no timeout settings</li>
     */
    public static ReactiveSqlSessionProfile DEFAULT_PROFILE = ReactiveSqlSessionProfile.builder().build();

    /**
     * Flag indicating whether auto-commit mode is enabled for this session profile.
     */
    private final boolean autoCommit;

    /**
     * The isolation level for database transactions in this session profile.
     * May be null if no specific isolation level is configured.
     */
    private final IsolationLevel isolationLevel;

    /**
     * Flag indicating whether transaction mode is enabled for this session profile.
     */
    private final boolean enableTransaction;

    /**
     * Flag indicating whether transactions should be forced to rollback.
     */
    private final boolean forceToRollback;

    /**
     * The maximum duration to wait for statement execution.
     * May be null if no statement timeout is configured.
     */
    private final Duration statementTimeout;

    /**
     * The maximum duration to wait for acquiring database locks.
     * May be null if no lock timeout is configured.
     */
    private final Duration lockTimeout;

    /**
     * Returns whether auto-commit mode is enabled for this session profile.
     *
     * @return true if auto-commit is enabled, false otherwise
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Returns the isolation level configured for this session profile.
     *
     * @return the isolation level, or null if not specified
     */
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    /**
     * Returns whether transaction mode is enabled for this session profile.
     *
     * @return true if transaction mode is enabled, false otherwise
     */
    public boolean isEnableTransaction() {
        return enableTransaction;
    }

    /**
     * Returns whether transactions should be forced to rollback.
     *
     * @return true if transactions should be forced to rollback, false otherwise
     */
    public boolean isForceToRollback() {
        return forceToRollback;
    }

    /**
     * Returns the statement timeout duration configured for this session profile.
     *
     * @return the statement timeout duration, or null if not specified
     */
    public Duration getStatementTimeout() {
        return statementTimeout;
    }

    /**
     * Returns the lock timeout duration configured for this session profile.
     *
     * @return the lock timeout duration, or null if not specified
     */
    public Duration getLockTimeout() {
        return lockTimeout;
    }

    /**
     * Constructs a new ReactiveSqlSessionProfile with the specified configuration parameters.
     * If transaction mode is enabled, auto-commit will be automatically disabled regardless
     * of the provided autoCommit parameter value.
     *
     * @param autoCommit        whether to enable auto-commit mode (will be overridden to false if enableTransaction is true)
     * @param isolationLevel    the isolation level for database transactions, may be null
     * @param enableTransaction whether to enable transaction mode
     * @param forceToRollback   whether to force transactions to rollback
     * @param statementTimeout  the maximum duration to wait for statement execution, may be null
     * @param lockTimeout       the maximum duration to wait for acquiring database locks, may be null
     */
    private ReactiveSqlSessionProfile(boolean autoCommit, IsolationLevel isolationLevel, boolean enableTransaction, boolean forceToRollback, Duration statementTimeout, Duration lockTimeout) {
        if (enableTransaction) {
            autoCommit = false;
        }
        this.autoCommit = autoCommit;
        this.isolationLevel = isolationLevel;
        this.enableTransaction = enableTransaction;
        this.forceToRollback = forceToRollback;
        this.statementTimeout = statementTimeout;
        this.lockTimeout = lockTimeout;
    }

    /**
     * Creates a new ReactiveSqlSessionProfile with default values.
     * Auto-commit mode is disabled, isolation level is set to null, transaction mode is disabled,
     * transactions are not forced to rollback, statement timeout is set to null, and lock timeout is set to null.
     */
    public static ReactiveSqlSessionProfileBuilder builder() {
        return new ReactiveSqlSessionProfileBuilder();
    }

    @Override
    public String toString() {
        return "ReactiveSqlSessionProfile{" +
                "autoCommit=" + autoCommit +
                ", isolationLevel=" + isolationLevel +
                ", enableTransaction=" + enableTransaction +
                ", forceToRollback=" + forceToRollback +
                ", statementTimeout=" + statementTimeout +
                ", lockTimeout=" + lockTimeout +
                '}';
    }

    /**
     * Builder class for creating ReactiveSqlSessionProfile instances.
     * Provides a fluent API for configuring reactive SQL session properties.
     */
    public static class ReactiveSqlSessionProfileBuilder {

        private boolean autoCommit = true;
        private IsolationLevel isolationLevel;
        private boolean enableTransaction;
        private boolean forceToRollback;
        private Duration statementTimeout;
        private Duration lockTimeout;

        /**
         * Disables auto-commit mode for the reactive SQL session.
         * When auto-commit is disabled, changes must be explicitly committed or rolled back.
         *
         * @return this Builder instance for method chaining
         */
        public ReactiveSqlSessionProfileBuilder disableAutoCommit() {
            this.autoCommit = true;
            return this;
        }

        /**
         * Sets the isolation level for the reactive SQL session.
         * This method configures the transaction isolation level and automatically enables transaction mode.
         * When an isolation level is set, the session will operate in transactional mode with the specified
         * isolation level applied to all database operations within the session.
         *
         * @param isolationLevel the isolation level to be used for database transactions. Must not be null.
         *                       Common values include READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, and SERIALIZABLE.
         * @return this ReactiveSqlSessionProfileBuilder instance for method chaining
         * @throws IllegalArgumentException if isolationLevel is null
         */
        public ReactiveSqlSessionProfileBuilder withIsolationLevel(IsolationLevel isolationLevel) {
            if (Objects.isNull(isolationLevel)) {
                throw new IllegalArgumentException("Isolation level must not be null");
            }
            this.enableTransaction = true;
            this.autoCommit = false;
            this.isolationLevel = isolationLevel;
            return this;
        }

        /**
         * Enables transaction mode for the reactive SQL session.
         * When transaction mode is enabled, auto-commit is automatically disabled.
         *
         * @return this Builder instance for method chaining
         */
        public ReactiveSqlSessionProfileBuilder enableTransaction() {
            this.enableTransaction = true;
            this.autoCommit = false;
            return this;
        }

        /**
         * Forces the session to rollback transactions.
         * When transaction mode is enabled, auto-commit is automatically disabled.
         *
         * @return this Builder instance for method chaining
         */
        public ReactiveSqlSessionProfileBuilder forceToRollback() {
            this.forceToRollback = true;
            this.autoCommit = false;
            return this;
        }

        /**
         * Sets the statement timeout duration for the reactive SQL session.
         *
         * @param statementTimeout the maximum duration to wait for statement execution
         * @return this Builder instance for method chaining
         * @throws IllegalArgumentException if statementTimeout is null or negative
         */
        public ReactiveSqlSessionProfileBuilder withStatementTimeout(Duration statementTimeout) {
            if (Objects.isNull(statementTimeout) || statementTimeout.isNegative()) {
                throw new IllegalArgumentException("Statement timeout must be a non-negative duration");
            }
            this.statementTimeout = statementTimeout;
            return this;
        }

        /**
         * Sets the lock timeout duration for the reactive SQL session.
         *
         * @param lockTimeout the maximum duration to wait for acquiring database locks
         * @return this Builder instance for method chaining
         * @throws IllegalArgumentException if lockTimeout is null or negative
         */
        public ReactiveSqlSessionProfileBuilder withLockTimeout(Duration lockTimeout) {
            if (Objects.isNull(lockTimeout) || lockTimeout.isNegative()) {
                throw new IllegalArgumentException("Lock timeout must be a non-negative duration");
            }
            this.lockTimeout = lockTimeout;
            return this;
        }

        /**
         * Builds and returns a new ReactiveSqlSessionProfile instance with the configured properties.
         *
         * @return a new ReactiveSqlSessionProfile instance configured with the builder's settings
         */
        public ReactiveSqlSessionProfile build() {
            return new ReactiveSqlSessionProfile(
                    this.autoCommit,
                    this.isolationLevel,
                    this.enableTransaction,
                    this.forceToRollback,
                    this.statementTimeout,
                    this.lockTimeout
            );
        }
    }
}
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
package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.support;

import io.r2dbc.spi.Connection;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.defaults.ReactiveSqlSessionProfile;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A context class that manages the execution state and resources for reactive MyBatis operations.
 * This class provides thread-safe management of R2DBC connections, transaction states, and logging
 * within a reactive SQL session. It maintains atomic references to ensure consistency in concurrent
 * environments and tracks various operational flags such as commit, rollback, and close requirements.
 *
 * @author Gang Cheng
 * @version 2.0.0
 */

public class ReactiveExecutorContext {

    private static final Log log = LogFactory.getLog(ReactiveExecutorContext.class);

    private final AtomicBoolean connectionBind = new AtomicBoolean(false);
    private final AtomicReference<Connection> currentConnection = new AtomicReference<>();
    private final AtomicBoolean requireToCommit = new AtomicBoolean(false);
    private final AtomicBoolean requireToRollback = new AtomicBoolean(false);
    private final AtomicBoolean requireToClose = new AtomicBoolean(false);
    private final AtomicBoolean inTransaction = new AtomicBoolean(false);
    private final AtomicBoolean isDirty = new AtomicBoolean(false);
    private final AtomicReference<R2dbcStatementLog> currentR2dbcStatementLog = new AtomicReference<>();
    private final ReactiveSqlSessionProfile reactiveSqlSessionProfile;

    /**
     * Constructs a new ReactiveExecutorContext with the specified reactive SQL session profile.
     *
     * @param reactiveSqlSessionProfile the reactive SQL session profile to be associated with this context
     */
    public ReactiveExecutorContext(ReactiveSqlSessionProfile reactiveSqlSessionProfile) {
        if (Objects.isNull(reactiveSqlSessionProfile)) {
            throw new IllegalArgumentException("reactiveSqlSessionProfile can't be null");
        }
        this.reactiveSqlSessionProfile = reactiveSqlSessionProfile;
        this.inTransaction.set(reactiveSqlSessionProfile.isEnableTransaction());
    }

    /**
     * Retrieves the current R2DBC statement log associated with this context.
     *
     * @return the current R2DBC statement log, or null if none is set
     */
    public R2dbcStatementLog getCurrentR2dbcStatementLog() {
        return this.currentR2dbcStatementLog.get();
    }

    /**
     * Marks this context as dirty, indicating that there are multiple statements executed within current context.
     * Once set to dirty, the context may allow additional connection binding attempts even when not in a transaction.
     */
    public void setDirty() {
        this.isDirty.set(true);
    }

    /**
     * Checks whether this context is marked as dirty.
     * A dirty context indicates that multiple statements have been executed within the current context,
     * which may allow additional connection binding attempts even when not in a transaction.
     *
     * @return true if the context is dirty, false otherwise
     */
    public boolean isDirty() {
        return this.isDirty.get();
    }

    /**
     * Sets the current R2DBC statement log for this context.
     *
     * @param r2dbcStatementLog the R2DBC statement log to be set as current
     * @throws IllegalArgumentException if the provided r2dbcStatementLog is null
     */
    public void withCurrentR2dbcStatementLog(R2dbcStatementLog r2dbcStatementLog) {
        if (Objects.isNull(r2dbcStatementLog)) {
            throw new IllegalArgumentException("r2dbcStatementLog can't be null");
        }
        this.currentR2dbcStatementLog.set(r2dbcStatementLog);
    }

    /**
     * Binds a new R2DBC connection to this context using the provided connection creator.
     * This method handles connection binding differently based on the transaction state:
     * <ul>
     *   <li>If in a transaction: allows multiple bind attempts, returning false for subsequent calls</li>
     *   <li>If not in a transaction: only allows one bind attempt, throwing an exception for subsequent calls</li>
     * </ul>
     * <p>
     * The method atomically checks and sets the connection binding state to ensure thread safety
     * in concurrent environments. Once a connection is successfully bound, it becomes the current
     * connection for this context.
     *
     * @param connectionCreator a Supplier that provides a Mono which emits the R2DBC connection to be bound;
     *                          must not be null and should return a Mono that emits a valid Connection instance
     * @return a Mono that emits {@code true} if the connection was successfully bound for the first time,
     * {@code false} if already bound and in a transaction or the context is dirty, or an error
     * if already bound without an active transaction and the context is not dirty
     * @throws IllegalStateException if connection is already bound and no transaction is enabled
     *                               and the context is not dirty (emitted through the Mono)
     */
    public Mono<Boolean> bindConnection(Supplier<Mono<? extends Connection>> connectionCreator) {
        if (connectionBind.compareAndSet(false, true)) {
            return Mono.fromSupplier(connectionCreator)
                    .flatMap(Mono::from)
                    .flatMap(connection -> Mono.fromCallable(() -> {
                        this.currentConnection.set(connection);
                        log.debug("[Bind connection] Bind new connection to context : " + connection);
                        return true;
                    }));
        } else if (!this.inTransaction.get() && !isDirty.get()) {
            return Mono.error(new IllegalStateException("Connection is already bound to this context and no transaction is enabled or the context is dirty"));
        }
        return Mono.just(false);
    }

    /**
     * Resets the context to its initial state by clearing the bound connection and resetting the dirty flag.
     * This method atomically unbinds any currently bound connection and clears the connection reference,
     * then resets the dirty flag to false. This operation is useful for cleaning up the context state
     * after completing operations or when preparing the context for reuse.
     * <p>
     * The method performs the following operations:
     * <ul>
     *   <li>Atomically checks if a connection is bound and unbinds it if present</li>
     *   <li>Clears the current connection reference</li>
     *   <li>Resets the dirty flag to false</li>
     * </ul>
     */
    public void reset() {
        log.debug("Reset reactive executor context");
        if (this.connectionBind.compareAndSet(true, false)) {
            this.currentConnection.getAndSet(null);
        }
        this.isDirty.set(false);
    }

    /**
     * Resets the commit requirement flag to false.
     * This method clears the flag that indicates whether a commit operation is required,
     * effectively canceling any pending commit requirement for this context.
     */
    public void resetRequireToCommit() {
        this.requireToCommit.set(false);
    }

    /**
     * Resets the rollback requirement flag to false.
     * This method clears the flag that indicates whether a rollback operation is required,
     * effectively canceling any pending rollback requirement for this context.
     */
    public void resetRequireToRollback() {
        this.requireToRollback.set(false);
    }

    /**
     * Retrieves the currently bound connection from this context.
     *
     * @return an Optional containing the current connection, or empty if no connection is bound
     */
    public Optional<Connection> getCurrentConnection() {
        return Optional.ofNullable(this.currentConnection.get());
    }

    /**
     * Retrieves the current SQL session profile associated with this context.
     *
     * @return the current SQL session profile that was provided during construction
     */
    public ReactiveSqlSessionProfile getCurrentSessionProfile() {
        return this.reactiveSqlSessionProfile;
    }

    /**
     * Marks this context as requiring a commit operation.
     * Sets the internal flag to indicate that a commit should be performed on the current transaction.
     */
    public void requireToCommit() {
        this.requireToCommit.set(true);
    }

    /**
     * Checks whether this context requires a commit operation.
     *
     * @return true if a commit is required, false otherwise
     */
    public boolean isRequireToCommit() {
        return this.requireToCommit.get();
    }

    /**
     * Marks this context as requiring a rollback operation.
     * Sets the internal flag to indicate that a rollback should be performed on the current transaction.
     */
    public void requireToRollback() {
        this.requireToRollback.set(true);
    }

    /**
     * Checks whether this context requires a rollback operation.
     *
     * @return true if a rollback is required, false otherwise
     */
    public boolean isRequireToRollback() {
        return this.requireToRollback.get();
    }

    /**
     * Marks this context as requiring a close operation.
     * Sets the internal flag to indicate t`hat resources should be closed.
     */
    public void requireToClose() {
        this.requireToClose.set(true);
    }

    /**
     * Checks whether this context requires a close operation.
     *
     * @return true if a close operation is required, false otherwise
     */
    public boolean isRequireToClose() {
        return this.requireToClose.get();
    }

    /**
     * Checks whether this context is currently within a transaction.
     *
     * @return true if the context is in a transaction, false otherwise
     */
    public boolean isInTransaction() {
        return this.inTransaction.get();
    }

    /**
     * Resets the context state with a new R2DBC statement log and restores default operational flags.
     * This method atomically updates the current statement log and resets all transaction-related
     * flags to their initial state. The transaction state is restored based on the session profile's
     * transaction enablement setting.
     *
     * @param r2dbcStatementLog the new R2DBC statement log to be associated with this context
     */
    public void resetWithR2dbcStatementLog(R2dbcStatementLog r2dbcStatementLog) {
        this.currentR2dbcStatementLog.set(r2dbcStatementLog);
        this.requireToCommit.set(false);
        this.requireToRollback.set(false);
        this.requireToClose.set(false);
        this.inTransaction.set(this.reactiveSqlSessionProfile.isEnableTransaction());
    }

    @Override
    public String toString() {
        return "ReactiveExecutorContext{" +
                "connectionBind=" + connectionBind +
                ", currentConnection=" + currentConnection +
                ", requireToCommit=" + requireToCommit +
                ", requireToRollback=" + requireToRollback +
                ", requireToClose=" + requireToClose +
                ", inTransaction=" + inTransaction +
                ", currentR2dbcStatementLog=" + currentR2dbcStatementLog +
                ", reactiveSqlSessionProfile=" + reactiveSqlSessionProfile +
                '}';
    }
}

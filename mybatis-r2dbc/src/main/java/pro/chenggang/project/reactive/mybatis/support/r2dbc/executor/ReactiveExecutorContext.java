package pro.chenggang.project.reactive.mybatis.support.r2dbc.executor;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.IsolationLevel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Reactive executor context.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12 /9/21.
 */
public class ReactiveExecutorContext {

    private final AtomicBoolean activeTransaction = new AtomicBoolean(false);
    private final AtomicReference<Connection> connectionReference = new AtomicReference<>();
    private final AtomicBoolean forceCommit = new AtomicBoolean(false);
    private final AtomicBoolean forceRollback = new AtomicBoolean(false);
    private final AtomicBoolean requireClosed = new AtomicBoolean(false);
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean withTransaction = new AtomicBoolean(false);
    private final boolean autoCommit;
    private final IsolationLevel isolationLevel;
    private StatementLogHelper statementLogHelper;

    /**
     * Instantiates a new Reactive executor context.
     *
     * @param autoCommit     the auto commit
     * @param isolationLevel the isolation level
     */
    public ReactiveExecutorContext(boolean autoCommit, IsolationLevel isolationLevel) {
        this.autoCommit = autoCommit;
        this.isolationLevel = isolationLevel;
    }

    /**
     * Is auto commit boolean.
     *
     * @return the boolean
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Is force commit boolean.
     *
     * @return the boolean
     */
    public boolean isForceCommit() {
        return forceCommit.get();
    }

    /**
     * Set force commit.
     *
     * @param forceCommit the force commit
     */
    public void setForceCommit(boolean forceCommit) {
        this.forceCommit.getAndSet(forceCommit);
    }

    /**
     * Is force rollback boolean.
     *
     * @return the boolean
     */
    public boolean isForceRollback() {
        return forceRollback.get();
    }

    /**
     * Set force rollback.
     *
     * @param forceRollback the force rollback
     */
    public void setForceRollback(boolean forceRollback) {
        this.forceRollback.getAndSet(forceRollback);
    }

    /**
     * Is dirty boolean.
     *
     * @return the boolean
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Set dirty.
     */
    public void setDirty() {
        this.dirty.compareAndSet(false, true);
    }

    /**
     * Reset dirty.
     */
    public void resetDirty() {
        this.dirty.compareAndSet(true, false);
    }

    /**
     * Set with transaction.
     */
    public void setWithTransaction() {
        this.withTransaction.compareAndSet(false, true);
    }

    /**
     * Reset with transaction.
     */
    public void resetWithTransaction() {
        this.withTransaction.compareAndSet(true, false);
    }

    /**
     * Is with transaction boolean.
     *
     * @return the boolean
     */
    public boolean isWithTransaction() {
        return this.withTransaction.get();
    }

    /**
     * Set active transaction boolean.
     *
     * @return the boolean
     */
    public boolean setActiveTransaction() {
        return this.activeTransaction.compareAndSet(false, true);
    }

    /**
     * Is require closed boolean.
     *
     * @return the boolean
     */
    public boolean isRequireClosed() {
        return this.requireClosed.get();
    }

    /**
     * Set require closed.
     *
     * @param requireClosed the require closed
     */
    public void setRequireClosed(boolean requireClosed) {
        this.requireClosed.getAndSet(requireClosed);
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
     * Gets statement log helper.
     *
     * @return the statement log helper
     */
    public StatementLogHelper getStatementLogHelper() {
        return statementLogHelper;
    }

    /**
     * Sets statement log helper.
     *
     * @param statementLogHelper the statement log helper
     */
    public void setStatementLogHelper(StatementLogHelper statementLogHelper) {
        this.statementLogHelper = statementLogHelper;
    }

    /**
     * Bind connection boolean.
     *
     * @param connection the connection
     * @return the boolean
     */
    public boolean bindConnection(Connection connection) {
        return this.connectionReference.compareAndSet(null, connection);
    }

    /**
     * Clear connection optional.
     *
     * @return the optional
     */
    public Optional<Connection> clearConnection() {
        return Optional.ofNullable(this.connectionReference.getAndSet(null));
    }

    /**
     * Gets connection.
     *
     * @return the connection
     */
    public Optional<Connection> getConnection() {
        return Optional.ofNullable(this.connectionReference.get());
    }

    @Override
    public String toString() {
        return "ReactiveExecutorContext [" +
                ", connectionReference=" + connectionReference +
                ", forceCommit=" + forceCommit +
                ", forceRollback=" + forceRollback +
                ", requireClosed=" + requireClosed +
                ", statementLogHelper=" + statementLogHelper +
                " ]";
    }
}

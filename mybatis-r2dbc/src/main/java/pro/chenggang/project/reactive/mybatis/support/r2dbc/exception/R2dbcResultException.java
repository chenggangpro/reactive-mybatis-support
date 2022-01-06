package pro.chenggang.project.reactive.mybatis.support.r2dbc.exception;

import io.r2dbc.spi.R2dbcException;

/**
 * The type R2dbc result exception.
 *
 * @author chenggang
 * @version 1.0.0
 * @date 12 /10/21.
 */
public class R2dbcResultException extends R2dbcException {

    private static final long serialVersionUID = 7651039074018197180L;

    /**
     * Instantiates a new R2dbc result exception.
     */
    public R2dbcResultException() {
        super();
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param reason the reason
     */
    public R2dbcResultException(String reason) {
        super(reason);
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param reason the reason
     * @param cause  the cause
     */
    public R2dbcResultException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Instantiates a new R2dbc result exception.
     *
     * @param cause the cause
     */
    public R2dbcResultException(Throwable cause) {
        super(cause);
    }
}

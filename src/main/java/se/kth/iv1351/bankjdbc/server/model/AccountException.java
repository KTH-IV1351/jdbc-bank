package se.kth.id1212.db.bankjdbc.server.model;

/**
 * Thrown when create, read or delete of an account fails.
 */
public class AccountException extends Exception {

    /**
     * Create a new instance thrown because of the specified reason.
     *
     * @param reason Why the exception was thrown.
     */
    public AccountException(String reason) {
        super(reason);
    }

    /**
     * Create a new instance thrown because of the specified reason and exception.
     *
     * @param reason Why the exception was thrown.
     * @param rootCause The exception that caused this exception to be thrown.
     */
    public AccountException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}

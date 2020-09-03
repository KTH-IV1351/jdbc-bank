package se.kth.id1212.db.bankjdbc.server.integration;

/**
 * Thrown when a call to the bank database fails.
 */
public class BankDBException extends Exception {

    /**
     * Create a new instance thrown because of the specified reason.
     *
     * @param reason Why the exception was thrown.
     */
    public BankDBException(String reason) {
        super(reason);
    }

    /**
     * Create a new instance thrown because of the specified reason and exception.
     *
     * @param reason Why the exception was thrown.
     * @param rootCause The exception that caused this exception to be thrown.
     */
    public BankDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}

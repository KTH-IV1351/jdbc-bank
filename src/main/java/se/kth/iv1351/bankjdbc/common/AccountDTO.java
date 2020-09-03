package se.kth.id1212.db.bankjdbc.common;

import java.io.Serializable;

/**
 * Specifies a read-only view of an account.
 */
public interface AccountDTO extends Serializable {
    /**
     * @return The balance.
     */
    public int getBalance();

    /**
     * @return The holder's name.
     */
    public String getHolderName();
}

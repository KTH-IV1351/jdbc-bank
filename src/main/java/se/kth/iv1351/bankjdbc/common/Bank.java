package se.kth.id1212.db.bankjdbc.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import se.kth.id1212.db.bankjdbc.server.model.AccountException;
import se.kth.id1212.db.bankjdbc.server.model.RejectedException;

/**
 * Specifies the bank's remote methods.
 */
public interface Bank extends Remote {
    /**
     * The default URI of the bank server in the RMI registry.
     */
    public static final String BANK_NAME_IN_REGISTRY = "bank";
    
    /**
     * Creates an account with the specified name and the balance zero.
     *
     * @param name The account holder's name.
     * @throws RemoteException  If unable to complete the RMI call.
     * @throws AccountException If unable to create the account.
     */
    public void createAccount(String name) throws RemoteException, AccountException;

    /**
     * Returns the account of the specified holder, or <code>null</code> if there is no such
     * account.
     *
     * @param holderName The holder whose account to search for.
     * @return The account of the specified holder, or <code>null</code> if there is no such
     *         account.
     * @throws RemoteException  If unable to complete the RMI call.
     * @throws AccountException If unable to search for the account.
     */
    public AccountDTO getAccount(String name) throws RemoteException, AccountException;

    /**
     * Deletes the specified account, if there is such an account. If there is no
     * such account, nothing happens.
     *
     * @param account The account to delete.
     * @throws RemoteException  If unable to complete the RMI call.
     * @throws AccountException If unable to delete account, or unable to check if there was an
     *                          account to delete.
     */
    public void deleteAccount(AccountDTO account) throws RemoteException, AccountException;

    /**
     * Lists all accounts in the bank.
     * 
     * @return A list of all accounts.
     * @throws RemoteException  If unable to complete the RMI call.
     * @throws AccountException If unable to list accounts.
     */
    public List<? extends AccountDTO> listAccounts() throws RemoteException, AccountException;

    /**
     * Deposits the specified amount to the specified account.
     *
     * @param acct   The account to which to deposit.
     * @param amount The amount to deposit.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If the specified amount is negative, or if unable to perform the
     *                           update.
     * @throws AccountException If unable to retrieve the specified account.
     */
    public void deposit(AccountDTO acct, int amt) throws RemoteException, RejectedException,
                                                         AccountException;

    /**
     * Withdraws the specified amount from the specified account.
     *
     * @param acct   The account from which to withdraw.
     * @param amount The amount to withdraw.
     * @throws RemoteException   If unable to complete the RMI call.
     * @throws RejectedException If the specified amount is negative, if the amount is larger than
     *                           the balance, or if unable to perform the update.
     * @throws AccountException If unable to retrieve the specified account.
     */
    public void withdraw(AccountDTO acct, int amt) throws RemoteException, RejectedException,
                                                          AccountException;
}

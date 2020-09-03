package se.kth.id1212.db.bankjdbc.server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import se.kth.id1212.db.bankjdbc.common.Bank;
import se.kth.id1212.db.bankjdbc.server.integration.BankDAO;
import se.kth.id1212.db.bankjdbc.server.integration.BankDBException;
import se.kth.id1212.db.bankjdbc.server.model.Account;
import se.kth.id1212.db.bankjdbc.common.AccountDTO;
import se.kth.id1212.db.bankjdbc.server.model.AccountException;
import se.kth.id1212.db.bankjdbc.server.model.RejectedException;

/**
 * Implementations of the bank's remote methods, this is the only server class that can be called
 * remotely
 */
public class Controller extends UnicastRemoteObject implements Bank {
    private final BankDAO bankDb;

    public Controller(String datasource, String dbms) throws RemoteException, BankDBException {
        super();
        bankDb = new BankDAO(dbms, datasource);
    }

    @Override
    public synchronized List<? extends AccountDTO> listAccounts() throws AccountException {
        try {
            return bankDb.findAllAccounts();
        } catch (Exception e) {
            throw new AccountException("Unable to list accounts.", e);
        }
    }

    @Override
    public synchronized void createAccount(String holderName) throws AccountException {
        String acctExistsMsg = "Account for: " + holderName + " already exists";
        String failureMsg = "Could not create account for: " + holderName;
        try {
            if (bankDb.findAccountByName(holderName) != null) {
                throw new AccountException(acctExistsMsg);
            }
            bankDb.createAccount(new Account(holderName, bankDb));
        } catch (Exception e) {
            throw new AccountException(failureMsg, e);
        }
    }

    @Override
    public synchronized AccountDTO getAccount(String holderName) throws AccountException {
        if (holderName == null) {
            return null;
        }

        try {
            return bankDb.findAccountByName(holderName);
        } catch (Exception e) {
            throw new AccountException("Could not search for account.", e);
        }
    }

    @Override
    public synchronized void deleteAccount(AccountDTO account) throws AccountException {
        try {
            bankDb.deleteAccount(account);
        } catch (Exception e) {
            throw new AccountException("Could not delete account: " + account, e);
        }
    }

    @Override
    public synchronized void deposit(AccountDTO acctDTO, int amt) throws RejectedException, AccountException {
        Account acct = (Account) getAccount(acctDTO.getHolderName());
        acct.deposit(amt);
    }

    @Override
    public synchronized void withdraw(AccountDTO acctDTO, int amt) throws RejectedException, AccountException {
        Account acct = (Account) getAccount(acctDTO.getHolderName());
        acct.withdraw(amt);
    }
}

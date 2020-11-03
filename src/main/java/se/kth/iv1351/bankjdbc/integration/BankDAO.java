/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Leif Lindbäck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so,subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.kth.iv1351.bankjdbc.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.kth.iv1351.bankjdbc.model.Account;
import se.kth.iv1351.bankjdbc.model.AccountDTO;

/**
 * This data access object (DAO) encapsulates all database calls in the bank
 * application. No code outside this class shall have any knowledge about the
 * database.
 */
public class BankDAO {
    private static final String HOLDER_TABLE_NAME = "holder";
    private static final String HOLDER_PK_COLUMN_NAME = "holder_id";
    private static final String HOLDER_COLUMN_NAME = "name";
    private static final String ACCT_TABLE_NAME = "account";
    private static final String ACCT_NO_COLUMN_NAME = "account_no";
    private static final String BALANCE_COLUMN_NAME = "balance";
    private static final String HOLDER_FK_COLUMN_NAME = HOLDER_PK_COLUMN_NAME;

    private Connection connection;
    private PreparedStatement createHolderStmt;
    private PreparedStatement findHolderPKStmt;
    private PreparedStatement createAccountStmt;
    private PreparedStatement findAccountByNameStmt;
    private PreparedStatement findAccountByAcctNoStmt;
    private PreparedStatement findAllAccountsStmt;
    private PreparedStatement deleteAccountStmt;
    private PreparedStatement changeBalanceStmt;

    /**
     * Constructs a new DAO object connected to the specified database.
     *
     * @param dbms       Database management system vendor. Currently supported
     *                   types are "derby" and "mysql".
     * @param datasource Database name.
     */
    public BankDAO() throws BankDBException {
        try {
            connectToBankDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            throw new BankDBException("Could not connect to datasource.", exception);
        }
    }

    /**
     * Creates a new account.
     *
     * @param account The account to create.
     * @throws BankDBException If failed to create the specified account.
     */
    public void createAccount(AccountDTO account) throws BankDBException {
        String failureMsg = "Could not create the account: " + account;
        int updatedRows = 0;
        try {
            int holderPK = findHolderPKByName(account.getHolderName());
            if (holderPK == 0) {
                createHolderStmt.setString(1, account.getHolderName());
                updatedRows = createHolderStmt.executeUpdate();
                if (updatedRows != 1) {
                    handleException(failureMsg, null);
                }
                holderPK = findHolderPKByName(account.getHolderName());
            }

            createAccountStmt.setInt(1, createAccountNo());
            createAccountStmt.setInt(2, account.getBalance());
            createAccountStmt.setInt(3, holderPK);
            updatedRows = createAccountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }

            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Searches for the account with the specified account number.
     *
     * @param acctNo The account number.
     * @return The account with the specified account number, or <code>null</code> if 
     *         there is no such account.
     * @throws BankDBException If failed to search for the account.
     */
    public Account findAccountByAcctNo(String acctNo) throws BankDBException {
        String failureMsg = "Could not search for specified account.";
        ResultSet result = null;
        try {
            findAccountByAcctNoStmt.setString(1, acctNo);
            result = findAccountByAcctNoStmt.executeQuery();
            if (result.next()) {
                return new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                   result.getString(HOLDER_COLUMN_NAME),
                                   result.getInt(BALANCE_COLUMN_NAME));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return null;
    }

    /**
     * Searches for all accounts whose holder has the specified name.
     *
     * @param holderName The account holder's name
     * @return A list with all accounts whose holder has the specified name, 
     *         the list is empty if there are no such account.
     * @throws BankDBException If failed to search for accounts.
     */
    public List<Account> findAccountsByHolder(String holderName) throws BankDBException {
        String failureMsg = "Could not search for specified accounts.";
        ResultSet result = null;
        List<Account> accounts = new ArrayList<>();
        try {
            findAccountByNameStmt.setString(1, holderName);
            result = findAccountByNameStmt.executeQuery();
            while (result.next()) {
                accounts.add(new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                         result.getString(HOLDER_COLUMN_NAME),
                                         result.getInt(BALANCE_COLUMN_NAME)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return accounts;
    }

    /**
     * Retrieves all existing accounts.
     *
     * @return A list with all existing accounts. The list is empty if there are no
     *         accounts.
     * @throws BankDBException If failed to search for accounts.
     */
    public List<Account> findAllAccounts() throws BankDBException {
        String failureMsg = "Could not list accounts.";
        List<Account> accounts = new ArrayList<>();
        try (ResultSet result = findAllAccountsStmt.executeQuery()) {
            while (result.next()) {
                accounts.add(new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                         result.getString(HOLDER_COLUMN_NAME),
                                         result.getInt(BALANCE_COLUMN_NAME)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
        return accounts;
    }

    /**
     * Changes the balance of the account with the number of the specified
     * <code>AccountDTO</code> object. The balance is set to the value in the specified
     * <code>AccountDTO</code>.
     *
     * @param account The account to update.
     * @throws BankDBException If unable to update the specified account.
     */
    public void updateAccount(AccountDTO account) throws BankDBException {
        String failureMsg = "Could not update the account: " + account;
        try {
            changeBalanceStmt.setInt(1, account.getBalance());
            changeBalanceStmt.setString(2, account.getAccountNo());
            int updatedRows = changeBalanceStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Deletes the account with the specified account number.
     *
     * @param acctNo The account to delete.
     * @throws BankDBException If unable to delete the specified account.
     */
    public void deleteAccount(String acctNo) throws BankDBException {
        String failureMsg = "Could not delete account: " + acctNo;
        try {
            deleteAccountStmt.setString(1, acctNo);
            int updatedRows = deleteAccountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    private void connectToBankDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bankdb",
                                                 "postgres", "postgres");
        // connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bankdb",
        //                                          "root", "javajava");
        connection.setAutoCommit(false);
    }

    private void prepareStatements() throws SQLException {
        createHolderStmt = connection.prepareStatement("INSERT INTO " + HOLDER_TABLE_NAME
            + "(" + HOLDER_COLUMN_NAME + ") VALUES (?)");

        createAccountStmt = connection.prepareStatement("INSERT INTO " + ACCT_TABLE_NAME
            + "(" + ACCT_NO_COLUMN_NAME + ", " + BALANCE_COLUMN_NAME + ", "
            + HOLDER_FK_COLUMN_NAME + ") VALUES (?, ?, ?)");

        findHolderPKStmt = connection.prepareStatement("SELECT " + HOLDER_PK_COLUMN_NAME
            + " FROM " + HOLDER_TABLE_NAME + " WHERE " + HOLDER_COLUMN_NAME + " = ?");

        findAccountByAcctNoStmt = connection.prepareStatement("SELECT a." + ACCT_NO_COLUMN_NAME
            + ", a." + BALANCE_COLUMN_NAME + ", h." + HOLDER_COLUMN_NAME + " from "
            + ACCT_TABLE_NAME + " a INNER JOIN " + HOLDER_TABLE_NAME + " h ON a."
            + HOLDER_FK_COLUMN_NAME + " = h." + HOLDER_PK_COLUMN_NAME + " WHERE a."
            + ACCT_NO_COLUMN_NAME + " = ?");

        findAccountByNameStmt = connection.prepareStatement("SELECT a." + ACCT_NO_COLUMN_NAME
            + ", a." + BALANCE_COLUMN_NAME + ", h." + HOLDER_COLUMN_NAME + " from "
            + ACCT_TABLE_NAME + " a INNER JOIN "
            + HOLDER_TABLE_NAME + " h ON a." + HOLDER_FK_COLUMN_NAME
            + " = h." + HOLDER_PK_COLUMN_NAME + " WHERE h." + HOLDER_COLUMN_NAME + " = ?");

        findAllAccountsStmt = connection.prepareStatement("SELECT h." + HOLDER_COLUMN_NAME
            + ", a." + ACCT_NO_COLUMN_NAME + ", a." + BALANCE_COLUMN_NAME + " FROM "
            + HOLDER_TABLE_NAME + " h INNER JOIN " + ACCT_TABLE_NAME + " a ON a."
            + HOLDER_FK_COLUMN_NAME + " = h." + HOLDER_PK_COLUMN_NAME);

        changeBalanceStmt = connection.prepareStatement("UPDATE " + ACCT_TABLE_NAME
            + " SET " + BALANCE_COLUMN_NAME + " = ? WHERE " + ACCT_NO_COLUMN_NAME + " = ? ");

        deleteAccountStmt = connection.prepareStatement("DELETE FROM " + ACCT_TABLE_NAME
            + " WHERE " + ACCT_NO_COLUMN_NAME + " = ?");
    }
    private void handleException(String failureMsg, Exception cause) throws BankDBException {
        String completeFailureMsg = failureMsg;
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            completeFailureMsg = completeFailureMsg + 
            ". Also failed to rollback transaction because of: " + rollbackExc.getMessage();
        }

        if (cause != null) {
            throw new BankDBException(failureMsg, cause);
        } else {
            throw new BankDBException(failureMsg);
        }
    }

    private void closeResultSet(String failureMsg, ResultSet result) throws BankDBException {
        try {
            result.close();
        } catch (Exception e) {
            throw new BankDBException(failureMsg + " Could not close result set.", e);
        }
    }

    private int createAccountNo() {
        return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
    }

    private int findHolderPKByName(String holderName) throws SQLException {
        ResultSet result = null;
        findHolderPKStmt.setString(1, holderName);
        result = findHolderPKStmt.executeQuery();
        if (result.next()) {
            return result.getInt(HOLDER_PK_COLUMN_NAME);
        }
        return 0;
    }
}

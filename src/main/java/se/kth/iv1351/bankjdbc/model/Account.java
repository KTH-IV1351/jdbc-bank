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

package se.kth.iv1351.bankjdbc.model;

import se.kth.iv1351.bankjdbc.common.AccountDTO;
import se.kth.iv1351.bankjdbc.integration.BankDAO;

/**
 * An account in the bank.
 */
public class Account implements AccountDTO {
    private int balance;
    private String holderName;
    private transient BankDAO bankDB;

    /**
     * Creates an account for the specified holder with the specified balance. The
     * account object will have a database connection.
     *
     * @param holderName The account holder's holderName.
     * @param balance    The initial balance.
     * @param bankDB     The DAO used to store updates to the database.
     */
    public Account(String holderName, int balance, BankDAO bankDB) {
        this.holderName = holderName;
        this.balance = balance;
        this.bankDB = bankDB;
    }

    /**
     * Creates an account for the specified holder with the specified balance. The
     * account object will not have a database connection.
     *
     * @param holderName The account holder's holderName.
     * @param balance    The initial balance.
     */
    public Account(String holderName, int balance) {
        this(holderName, balance, null);
    }

    /**
     * Creates an account for the specified holder with the balance zero.
     *
     * @param holderName The account holder's holderName.
     * @param bankDB     The DAO used to store updates to the database.
     */
    public Account(String name, BankDAO bankDB) {
        this(name, 0, bankDB);
    }

    /**
     * Deposits the specified amount.
     *
     * @param amount The amount to deposit.
     * @throws AccountException If the specified amount is negative, or if unable to
     *                          perform the update.
     */
    public void deposit(int amount) throws RejectedException {
        if (amount < 0) {
            throw new RejectedException(
                    "Tried to deposit negative value, illegal value: " + amount + "." + accountInfo());
        }
        changeBalance(balance + amount, "Could not deposit.");
    }

    /**
     * Withdraws the specified amount.
     *
     * @param amount The amount to withdraw.
     * @throws AccountException If the specified amount is negative, if the amount
     *                          is larger than the balance, or if unable to perform
     *                          the update.
     */
    public void withdraw(int amount) throws RejectedException {
        if (amount < 0) {
            throw new RejectedException(
                    "Tried to withdraw negative value, illegal value: " + amount + "." + accountInfo());
        }
        if (balance - amount < 0) {
            throw new RejectedException("Tried to overdraft, illegal value: " + amount + "." + accountInfo());
        }
        changeBalance(balance - amount, "Could not withdraw.");
    }

    private void changeBalance(int newBalance, String failureMsg) throws RejectedException {
        int initialBalance = balance;
        try {
            balance = newBalance;
            bankDB.updateAccount(this);
        } catch (Exception e) {
            balance = initialBalance;
            throw new RejectedException(failureMsg + accountInfo(), e);
        }
    }

    private String accountInfo() {
        return " " + this;
    }

    /**
     * @return The balance.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * @return The holder's name.
     */
    public String getHolderName() {
        return holderName;
    }

    /**
     * @return A string representation of all fields in this object.
     */
    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.append("Account: [");
        stringRepresentation.append("holder: ");
        stringRepresentation.append(holderName);
        stringRepresentation.append(", balance: ");
        stringRepresentation.append(balance);
        stringRepresentation.append("]");
        return stringRepresentation.toString();
    }
}

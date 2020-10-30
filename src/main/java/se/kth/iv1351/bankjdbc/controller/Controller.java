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

package se.kth.iv1351.bankjdbc.controller;

import java.util.List;

import se.kth.iv1351.bankjdbc.common.AccountDTO;
import se.kth.iv1351.bankjdbc.integration.BankDAO;
import se.kth.iv1351.bankjdbc.integration.BankDBException;
import se.kth.iv1351.bankjdbc.model.Account;
import se.kth.iv1351.bankjdbc.model.AccountException;
import se.kth.iv1351.bankjdbc.model.RejectedException;

/**
 * Implementations of the bank's remote methods, this is the only server class
 * that can be called remotely
 */
public class Controller {
    private final BankDAO bankDb;

    public Controller() throws BankDBException {
        bankDb = new BankDAO();
    }

    public synchronized List<? extends AccountDTO> listAccounts() throws AccountException {
        try {
            return bankDb.findAllAccounts();
        } catch (Exception e) {
            throw new AccountException("Unable to list accounts.", e);
        }
    }

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

    public synchronized void deleteAccount(AccountDTO account) throws AccountException {
        try {
            bankDb.deleteAccount(account);
        } catch (Exception e) {
            throw new AccountException("Could not delete account: " + account, e);
        }
    }

    public synchronized void deposit(AccountDTO acctDTO, int amt) throws RejectedException, AccountException {
        Account acct = (Account) getAccount(acctDTO.getHolderName());
        acct.deposit(amt);
    }

    public synchronized void withdraw(AccountDTO acctDTO, int amt) throws RejectedException, AccountException {
        Account acct = (Account) getAccount(acctDTO.getHolderName());
        acct.withdraw(amt);
    }
}

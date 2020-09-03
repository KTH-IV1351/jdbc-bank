/*
 * The MIT License
 *
 * Copyright 2017 Leif Lindbäck <leifl@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
package se.kth.id1212.db.bankjdbc.client.view;

import java.util.List;
import java.util.Scanner;
import se.kth.id1212.db.bankjdbc.common.Bank;
import se.kth.id1212.db.bankjdbc.common.AccountDTO;

/**
 * Reads and interprets user commands. The command interpreter will run in a separate thread, which
 * is started by calling the <code>start</code> method. Commands are executed in a thread pool, a
 * new prompt will be displayed as soon as a command is submitted to the pool, without waiting for
 * command execution to complete.
 */
public class NonBlockingInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();
    private Bank bank;
    private boolean receivingCmds = false;

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     *
     * @param server The server with which this chat client will communicate.
     */
    public void start(Bank bank) {
        this.bank = bank;
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands.
     */
    @Override
    public void run() {
        AccountDTO acct = null;
        while (receivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case HELP:
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println(command.toString().toLowerCase());
                        }
                        break;
                    case QUIT:
                        receivingCmds = false;
                        break;
                    case NEW:
                        bank.createAccount(cmdLine.getParameter(0));
                        ;
                        break;
                    case DELETE:
                        acct = bank.getAccount(cmdLine.getParameter(0));
                        bank.deleteAccount(acct);
                        break;
                    case LIST:
                        List<? extends AccountDTO> accounts = bank.listAccounts();
                        for (AccountDTO account : accounts) {
                            outMgr.println(account.getHolderName() + ": " + account.getBalance());
                        }
                        break;
                    case DEPOSIT:
                        acct = bank.getAccount(cmdLine.getParameter(0));
                        bank.deposit(acct, Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case WITHDRAW:
                        acct = bank.getAccount(cmdLine.getParameter(0));
                        bank.withdraw(acct, Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case BALANCE:
                        acct = bank.getAccount(cmdLine.getParameter(0));
                        outMgr.println(Integer.toString(acct.getBalance()));
                        break;
                    default:
                        outMgr.println("illegal command");
                }
            } catch (Exception e) {
                outMgr.println("Operation failed");
                outMgr.println(e.getMessage());
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }
}

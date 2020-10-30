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

package se.kth.iv1351.bankjdbc.view;

import java.util.List;
import java.util.Scanner;

import se.kth.iv1351.bankjdbc.common.AccountDTO;
import se.kth.iv1351.bankjdbc.controller.Controller;

/**
 * Reads and interprets user commands. The command interpreter will run in a
 * separate thread, which is started by calling the <code>start</code> method.
 * Commands are executed in a thread pool, a new prompt will be displayed as
 * soon as a command is submitted to the pool, without waiting for command
 * execution to complete.
 */
public class BlockingInterpreter {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when
     * this method returns. Calling <code>start</code> on an interpreter that is
     * already started has no effect.
     *
     * @param server The server with which this chat client will communicate.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
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
                        keepReceivingCmds = false;
                        break;
                    case NEW:
                        ctrl.createAccount(cmdLine.getParameter(0));
                        break;
                    case DELETE:
                        ctrl.deleteAccount(ctrl.getAccount(cmdLine.getParameter(0)));
                        break;
                    case LIST:
                        List<? extends AccountDTO> accounts = ctrl.listAccounts();
                        for (AccountDTO account : accounts) {
                            System.out.println(account.getHolderName() + ": " + account.getBalance());
                        }
                        break;
                    case DEPOSIT:
                        AccountDTO acctForDep = ctrl.getAccount(cmdLine.getParameter(0));
                        ctrl.deposit(acctForDep, Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case WITHDRAW:
                        AccountDTO acctForWith = ctrl.getAccount(cmdLine.getParameter(0));
                        ctrl.withdraw(acctForWith, Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case BALANCE:
                        System.out.println(Integer.toString(ctrl.getAccount(cmdLine.getParameter(0)).getBalance()));
                        break;
                    default:
                        System.out.println("illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
            }
        }
    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine();
    }
}

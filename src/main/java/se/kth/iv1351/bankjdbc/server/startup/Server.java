package se.kth.iv1351.bankjdbc.server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import se.kth.iv1351.bankjdbc.common.Bank;
import se.kth.iv1351.bankjdbc.server.controller.Controller;
import se.kth.iv1351.bankjdbc.server.integration.BankDBException;

/**
 * Starts the bank server.
 */
public class Server {
    private static final String USAGE = "java bankjdbc.Server [bank name in rmi registry] "
            + "[bank database name] [dbms: derby or mysql]";
    private String bankName = Bank.BANK_NAME_IN_REGISTRY;
    private String datasource = "Bank";
    private String dbms = "derby";

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.parseCommandLineArgs(args);
            server.startRMIServant();
            System.out.println("Bank server started.");
        } catch (RemoteException | MalformedURLException | BankDBException e) {
            System.out.println("Failed to start bank server.");
        }
    }

    private void startRMIServant() throws RemoteException, MalformedURLException, BankDBException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        Controller contr = new Controller(datasource, dbms);
        Naming.rebind(bankName, contr);
    }

    private void parseCommandLineArgs(String[] args) {
        if (args.length > 3 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        if (args.length > 0) {
            bankName = args[0];
        }

        if (args.length > 1) {
            datasource = args[1];
        }

        if (args.length > 2) {
            dbms = args[2];
        }
    }
}

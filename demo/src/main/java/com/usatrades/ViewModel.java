package com.usatrades;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Comparator;

public class ViewModel {
    private ArrayList<File> files = new ArrayList<>();
    private ArrayList<Trade> trades = new ArrayList<>();
    private ArrayList<Client> clients = new ArrayList<>();
    private Parser parser = new Parser();
    private HashMap<String, Integer> securitiesMap = new HashMap<>();
    private File clientFile;
    private File securitiesFile;

    public ViewModel(File clientFile, File securitiesFile) {
        this.clientFile = clientFile;
        this.securitiesFile = securitiesFile;
        // Read clients
        clients = readClients(clientFile);
        // Read securities
        securitiesMap = readSecurities(securitiesFile);
    }

    // if Main could not find files
    public ViewModel() {
        clientFile = null;
        securitiesFile = null;
    }

    // check if clientFile not found
    public boolean clientFileFound() {
        if (clientFile == null) {
            return false;
        } else {
            return true;
        }
    }

    //
    // check if securitiesFile not found
    public boolean securitiesFileFound() {
        if (securitiesFile == null) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<Client> readClients(File file) {
        try {
            Scanner scanner = new Scanner(file);
            ArrayList<Client> clientList = new ArrayList<Client>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // skip empty lines
                if (line.equals("")) {
                    continue;
                }
                String[] parts = line.split(",");
                String name = parts[0];
                int account_nr = Integer.parseInt(parts[1]);
                clientList.add(new Client(name, account_nr));
            }
            scanner.close();

            // Sort the clients by name
            clientList.sort(Comparator.comparing(Client::getName));

            clientFile = file;

            return clientList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, Integer> readSecurities(File file) {
        try {
            Scanner scanner = new Scanner(file);

            HashMap<String, Integer> map = new HashMap<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String isin = parts[1];
                Integer smid = Integer.parseInt(parts[2]);
                map.put(isin, smid);
            }
            scanner.close();

            securitiesFile = file;

            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public ArrayList<Trade> getTrades() {
        return trades;
    }

    // add trade file from Instinet, Beech Hill or RJO Brien
    public void addFile(File file) {
        files.add(file);

        // Parse the file
        ArrayList<Trade> newTrades = parser.parse(file);

        // find smid for all trades
        for (Trade trade : newTrades) {
            // if security does not exist
            Integer smid = securitiesMap.get(trade.getIsin());
            if (smid != null) {
                trade.setSmid(securitiesMap.get(trade.getIsin()));
            }
        }

        trades.addAll(newTrades);
    }

    public int findAccountNr(String accountName) {
        for (Client client : clients) {
            if (client.getName().equals(accountName)) {
                return client.getAccount_nr();
            }
        }

        return -1; // Account not found
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public String[] getClientNames() {
        String[] names = new String[clients.size()];
        for (int i = 0; i < clients.size(); i++) {
            names[i] = clients.get(i).getName();
        }
        return names;
    }

    public String generateOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Reference\tCounterparty\tRisk Counterparty\tBook\tPrimary Security (GUI)\tNumber of Shares\tPrice\tTrade Date\tValue Date\tClient Commission Rate (0-100%)\n");
        for (Trade trade : trades) {
            sb.append(trade.output()).append("\n");
        }
        return sb.toString();
    }

    public void clearTrades() {
        trades.clear();
    }

    public void clearFiles() {
        files.clear();
    }

    public void addClient(String clientName, int clientAccountNr) {
        // Needs client name
        if (clientName.equals("")) {
            return;
        }
        Client newClient = new Client(clientName, clientAccountNr);

        clients.add(newClient);
        // sort clients with newClient
        clients.sort(Comparator.comparing(Client::getName));

        // write new client to file
        try {
            FileWriter writer = new FileWriter(clientFile, true);
            BufferedWriter bw = new BufferedWriter(writer);

            bw.write(clientName + "," + clientAccountNr);
            bw.newLine();
            bw.close();
            System.out.println("Added new client: " + newClient.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSecurity(String ticker, String isin, int smid) {
        Security newSecurity = new Security(ticker, isin, smid);

        // update existing trades with this security
        for (Trade trade : trades) {
            if (trade.getIsin().equals(isin)) {
                trade.setSmid(smid);
            }
        }

        // write new security to file
        try {
            FileWriter writer = new FileWriter(securitiesFile, true);
            BufferedWriter bw = new BufferedWriter(writer);

            bw.write(ticker + "," + isin + "," + smid);
            bw.newLine();
            bw.close();
            System.out.println("Added new security: " + newSecurity.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.usatrades;

public class Client {
    private String name;
    private int account_nr;

    public Client(String name, int account_nr) {
        this.name = name;
        this.account_nr = account_nr;
    }

    public String getName() {
        return name;
    }

    public int getAccount_nr() {
        return account_nr;
    }

    @Override
    public String toString() {
        return name + ": " + account_nr;
    }
}

package com.usatrades;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // String clientPath = "J:\\BackOfficeApps\\USA-booking\\kunder.csv";
        // String securitiesPath = "J:\\BackOfficeApps\\USA-booking\\papirer.csv";
        String clientPath = "kunder.csv";
        String securitiesPath = "papirer.csv";

        File clientFile = new File(clientPath);
        File securitiesFile = new File(securitiesPath);

        ViewModel viewModel = null;
        // if both files exist
        if (clientFile.exists() && securitiesFile.exists()) {
            viewModel = new ViewModel(clientFile, securitiesFile);
        } else {
            viewModel = new ViewModel();
        }

        GUI gui = new GUI(viewModel);
    }
}

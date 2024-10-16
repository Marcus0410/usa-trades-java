package com.usatrades;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String clientPath = "/Users/marcus/Desktop/egne_prosjekter/usa-trades-java/demo/src/main/java/com/usatrades/kunder.csv";
        String securitiesPath = "/Users/marcus/Desktop/egne_prosjekter/usa-trades-java/demo/src/main/java/com/usatrades/papirer.csv";

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

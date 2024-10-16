package com.usatrades;

public class Security {
    private int smid;
    private String isin;
    private String ticker;

    public Security(String ticker, String isin, int smid) {
        this.ticker = ticker;
        this.isin = isin;
        this.smid = smid;
    }

    @Override
    public String toString() {
        return ticker + "," + isin + "," + smid;
    }
}
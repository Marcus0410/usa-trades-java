package com.usatrades;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Trade {
    private int accountNr;
    private int brokerAccountNr;
    private int smid;
    private String isin;
    private int qty;
    private LocalDate tradeDate;
    private LocalDate settleDate;
    private double commission;
    private char side; // B = buy, S = sell
    private String ticker;
    private double fees;
    private double netAmount;
    private double netPrice;
    private double commissionFromBroker;

    public Trade(int qty, LocalDate trade_date, LocalDate settle_date, String isin, double commission, char side,
            String ticker, double fees, double netAmount, double netPrice, int brokerAccountNr) {
        this.smid = 0;
        this.isin = isin;
        qty = Math.abs(qty); // first make qty positive
        // if buy qty is negative
        if (side == 'B') {
            this.qty = -qty;
        } else {
            this.qty = qty;
        }
        this.tradeDate = trade_date;
        this.settleDate = settle_date;
        this.commission = commission;
        this.side = side;
        this.ticker = ticker;
        this.fees = fees;
        this.netAmount = netAmount;
        this.netPrice = netPrice;
        this.brokerAccountNr = brokerAccountNr;
    }

    public double get_output_price() {
        double output_price = (fees + netAmount) / Math.abs(qty);
        assert output_price > 0 : "output_price was negative.";
        // round to 4 decimal places
        BigDecimal roundedValue = BigDecimal.valueOf(output_price).setScale(4, RoundingMode.HALF_UP);

        return roundedValue.doubleValue();
    }

    public void setSmid(int smid) {
        this.smid = smid;
    }

    public double getCommissionFromBroker() {
        return commissionFromBroker;
    }

    public int getAccountNr() {
        return accountNr;
    }

    public void set_account_nr(int account_nr) {
        this.accountNr = account_nr;
    }

    public void setCommissionFromBroker(double commission) {
        this.commissionFromBroker = commission;
    }

    public String output() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String td = tradeDate.format(formatter);
        String sd = settleDate.format(formatter);
        String comm = "";
        if (commissionFromBroker != 0.0) {
            comm = "" + commissionFromBroker;
        }
        int brokerQty = -qty;
        StringBuilder sb = new StringBuilder();
        // client side
        // TODO: Dobbeltsjekk at output er riktig når du limer inn i Inferno
        sb.append("\t" + accountNr + "\t\t21\t" + smid + "\t" + qty + "\t" + get_output_price() + "\t" + td + "\t"
                + sd + comm + "\n");
        // broker side
        sb.append("\t" + brokerAccountNr + "\t\t21\t" + smid + "\t" + brokerQty + "\t" + get_output_price() + "\t" + td
                + "\t" + sd);
        return sb.toString();
    }

    @Override
    public String toString() {
        return side + " " + ticker + " " + Math.abs(qty) + " @ " + netPrice;
    }

    public String getIsin() {
        return isin;
    }
}

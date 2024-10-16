package com.usatrades;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For .xlsx files
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Parser {
    public ArrayList<Trade> parse(File file) {
        if (file.getName().equals("CLOSING CONFIRMATION REPORT - ABG.xls")) {
            return readBeechHill(file);
        } else if (file.getName().contains("E28855")) {
            return read_instinet(file);
        } else if (file.getName().contains("Equities Invoice from RJOBRIEN to ABG SUNDAL COLLIER ASA")) {
            return read_rjobrien(file);
        } else {
            return new ArrayList<Trade>(); // return empty list
        }
    }

    private ArrayList<Trade> read_instinet(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);

            // Create Workbook instance holding reference to Excel file
            Workbook workbook = new XSSFWorkbook(inputStream);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            ArrayList<Trade> trades = new ArrayList<>();

            // Iterate over trades
            for (Row row : sheet) {
                // Skip first row with headers
                Cell c = row.getCell(0);
                if (c.getStringCellValue().equals("NAME")) {
                    continue;
                }

                // Initialize variables
                LocalDate tradeDate = null;
                LocalDate settleDate = null;
                String ticker = "";
                String isin = "";
                char side = ' ';
                int quantity = 0;
                double netPrice = 0.0;
                double commission = 0.0;
                double netAmount = 0.0;
                double fees = 0.0;

                // Iterate over all cells
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    switch (i) {
                        case 4:
                            ticker = cell.getStringCellValue();
                            break;
                        case 28:
                            isin = cell.getStringCellValue();
                            break;
                        case 9:
                            side = cell.getStringCellValue().charAt(0);
                            break;
                        case 10:
                            quantity = (int) cell.getNumericCellValue();
                            break;
                        case 11:
                            // no net price on instinet file (using gross price instead)
                            netPrice = cell.getNumericCellValue();
                            break;
                        case 17:
                            fees = cell.getNumericCellValue();
                            break;
                        case 22:
                            netAmount = cell.getNumericCellValue();
                            break;
                        case 13:
                            tradeDate = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            break;
                        case 14:
                            settleDate = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            break;
                    }
                }

                trades.add(new Trade(quantity, tradeDate, settleDate, isin, commission, side, ticker, fees, netAmount,
                        netPrice, 403043));
            }

            workbook.close();
            inputStream.close();

            return trades;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Trade> read_rjobrien(File file) {
        try {
            // get text from PDF
            PDDocument doc = Loader.loadPDF(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            doc.close();

            String[] lines = text.split("\n");

            // Initialize variables
            LocalDate tradeDate = null;
            LocalDate settleDate = null;
            String ticker = "";
            String isin = "";
            char side = ' ';
            int quantity = 0;
            double netPrice = 0.0;
            double commission = 0.0;
            double netAmount = 0.0;
            double fees = 0.0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // get dates
                if (line.equals("Trader:")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.US);
                    tradeDate = LocalDate.parse(lines[i + 1].trim(), formatter);
                    settleDate = LocalDate.parse(lines[i + 2].trim(), formatter);
                }
                // get ticker and isin
                else if (line.equals("ISIN:")) {
                    ticker = lines[i + 1].trim();
                    isin = lines[i + 2].trim();
                }
                // get quanitity
                else if (line.contains("Quantity:")) {
                    quantity = Integer.parseInt(line.split(":")[1].trim().replace(",", ""));
                }
                // get side
                else if (line.equals("Counterparty:")) {
                    if (lines[i + 1].trim().equals("BUY")) {
                        side = 'B';
                    } else {
                        side = 'S';
                    }
                }
                // get net price, commission, fees and net amount
                else if (line.contains("Order:")) {
                    fees = Double.parseDouble(lines[i + 2].trim().split(" ")[0].trim().replace(",", ""));
                    netPrice = Double.parseDouble(lines[i + 3].trim().split(" ")[0].trim().replace(",", ""));
                    commission = Double.parseDouble(lines[i + 5].trim().split(" ")[0].trim().replace(",", ""));
                }

            }
            netAmount = netPrice * quantity;

            Trade trade = new Trade(quantity, tradeDate, settleDate, isin, commission, side, ticker, fees, netAmount,
                    netPrice, 421058);

            ArrayList<Trade> trades = new ArrayList<Trade>();

            trades.add(trade);

            return trades;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read trades from a file from Beech Hill.
     *
     * @param file A .xls file from Beech Hill
     * @return A list of Trade objects
     */
    private ArrayList<Trade> readBeechHill(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);

            // Create Workbook instance holding reference to Excel file
            Workbook workbook = new HSSFWorkbook(inputStream);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Get trade date and settlement date
            Row datesRow = sheet.getRow(9);

            // Extract dates
            Date td = datesRow.getCell(1).getDateCellValue();
            Date sd = datesRow.getCell(2).getDateCellValue();
            LocalDate tradeDate = td.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate settleDate = sd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            ArrayList<Trade> trades = new ArrayList<>();

            // Iterate over trades
            for (Row row : sheet) {
                // Skip all non-trade rows
                Cell c = row.getCell(0);
                if (row.getRowNum() < 15 || c == null || c.getCellType() == CellType.BLANK) {
                    continue;
                }

                // Initialize variables
                String ticker = "";
                String isin = "";
                char side = ' ';
                int quantity = 0;
                double netPrice = 0.0;
                double commission = 0.0;
                double netAmount = 0.0;
                double fees = 0.0;

                // Iterate over all cells
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    switch (i) {
                        case 0:
                            ticker = cell.getStringCellValue();
                            break;
                        case 1:
                            isin = cell.getStringCellValue();
                            break;
                        case 2:
                            if (cell.getStringCellValue().contains("BUY")) {
                                side = 'B';
                            } else {
                                side = 'S';
                            }
                            break;
                        case 3:
                            quantity = (int) cell.getNumericCellValue();
                            break;
                        case 4:
                            netPrice = cell.getNumericCellValue();
                            break;
                        case 6:
                            fees = cell.getNumericCellValue();
                            break;
                        case 7:
                            netAmount = cell.getNumericCellValue();
                            break;
                    }
                }

                trades.add(new Trade(quantity, tradeDate, settleDate, isin, commission, side, ticker, fees, netAmount,
                        netPrice, 400860));
            }

            workbook.close();
            inputStream.close();

            return trades;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

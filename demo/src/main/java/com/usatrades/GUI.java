package com.usatrades;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUI {

    private JLabel filesLabel; // Text element to show dropped files
    private ViewModel viewModel;
    private JScrollPane tradesTable; // Table element to show trades
    private JTextArea outputArea; // Text element to show output

    public GUI(ViewModel model) {
        viewModel = model;

        // if client and securities files are not found, show pop-ups
        if (viewModel.clientFileFound() == false || viewModel.securitiesFileFound() == false) {
            selectFilePaths(viewModel);
        }

        // Create a JFrame to hold the GUI panel
        JFrame frame = new JFrame("USA Booking");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout()); // Use BorderLayout to arrange components

        // Add the GUI panel to the frame
        frame.add(panel);

        // Create a label to display text underneath the drop area
        filesLabel = new JLabel("Ingen filer laster opp enda.", JLabel.CENTER);
        filesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        filesLabel.setForeground(Color.GRAY);

        // Create the output label
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputPane = new JScrollPane(outputArea);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        // Create the table
        JTable table = createTable();
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        tradesTable = new JScrollPane(table);

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        // button to clear table
        JButton clearButton = new JButton("Reset");
        clearButton.addActionListener(e -> clearTable(viewModel, tableModel));
        buttonsPanel.add(clearButton);

        // button to copy output to clipboard
        JButton copyButton = new JButton("Kopier");
        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(outputArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        buttonsPanel.add(copyButton);

        // add menu for adding new client
        buttonsPanel.add(newClientPanel());
        // create panel for adding new security
        buttonsPanel.add(newSecurityPanel());

        JPanel tableAndOutputPanel = new JPanel();
        tableAndOutputPanel.setLayout(new BoxLayout(tableAndOutputPanel, BoxLayout.Y_AXIS));

        // Add components to the panel
        panel.add(filesLabel, BorderLayout.PAGE_END); // The text element at the bottom
        panel.add(buttonsPanel, BorderLayout.LINE_END); // The button at the bottom
        tableAndOutputPanel.add(tradesTable);
        tableAndOutputPanel.add(outputPane);
        panel.add(tableAndOutputPanel, BorderLayout.CENTER);

        // Set up the drop target for the panel
        frame.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    // Accept the drop
                    dtde.acceptDrop(dtde.getDropAction());

                    // Get the dropped files
                    @SuppressWarnings("unchecked")
                    List<File> droppedFileList = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : droppedFileList) {
                        viewModel.addFile(file);
                    }

                    // Process the files and update the infoLabel
                    StringBuilder fileNames = new StringBuilder("Filer lastet opp:\n");
                    for (File file : viewModel.getFiles()) {
                        fileNames.append(file.getName()).append("\n");
                    }

                    // Update the text element (infoLabel) with file names
                    filesLabel.setText("<html>" + fileNames.toString().replace("\n", "<br>") + "</html>");

                    // Update table
                    tableModel.setRowCount(0);
                    ArrayList<Trade> trades = viewModel.getTrades();
                    for (Trade trade : trades) {
                        tableModel.addRow(new Object[] { trade.toString(), "-Kunde-" });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Make the frame visible
        frame.setVisible(true);
    }

    // creates a table over the uploaded trades
    private JTable createTable() {
        // data for table
        String[] columnNames = { "Trade", "Kunde", "Kurtasje (bps)" };
        ArrayList<Trade> trades = viewModel.getTrades();

        Object[][] data = new Object[trades.size()][3];
        for (int i = 0; i < trades.size(); i++) {
            data[i][0] = trades.get(i).toString();
            data[i][1] = "-Kunde-";
            data[i][2] = "";
        }

        // Create the table model with specific editable logic
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the second and third columns editable (columns 1 and 2)
                return column != 0;
            }
        };

        // Listen for changes in the table and update Trade object fields
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                // Check if the change is in column 1 (List 1) or column 2 (List 2)
                if (column == 1) {
                    String selected = (String) tableModel.getValueAt(row, column);
                    int account_nr = viewModel.findAccountNr(selected);

                    assert account_nr != -1;

                    trades.get(row).set_account_nr(account_nr);
                } else if (column == 2) {
                    String selected = (String) tableModel.getValueAt(row, column);
                    int bps = Integer.parseInt(selected);

                    double commission = bps / 100.0;
                    trades.get(row).setCommissionFromBroker(commission);
                }

                // update output
                showOutput(outputArea);
            }
        });

        // Create the table
        JTable table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        table.setRowHeight(30);

        // Create drowdown for client (account_nr)
        String[] clients = viewModel.getClientNames();
        JComboBox<String> comboBox1 = new JComboBox<>(clients);
        TableCellEditor editor1 = new DefaultCellEditor(comboBox1);
        table.getColumnModel().getColumn(1).setCellEditor(editor1);

        return table;
    }

    private JPanel newClientPanel() {
        // create panel for adding new client
        JPanel addClientPanel = new JPanel();
        addClientPanel.setLayout(new GridLayout(0, 1));

        // addClient component
        JLabel addClientLabel = new JLabel();
        addClientLabel.setText("Legg til ny kunde:");
        addClientLabel.setFont(new Font("Arial", Font.BOLD, 15));
        JTextField clientNameTextField = new JTextField();
        JTextField clientAccountNrTextField = new JTextField();
        JButton addClientButton = new JButton("Legg til");

        addClientButton.addActionListener(e -> {
            String clientName = clientNameTextField.getText().trim();
            int clientAccountNr = Integer.parseInt(clientAccountNrTextField.getText().trim());

            viewModel.addClient(clientName, clientAccountNr);
            // clear input fiels
            clientNameTextField.setText("");
            clientAccountNrTextField.setText("");
            // update table with new client
            tradesTable = new JScrollPane(createTable());
        });

        // add components to addClientPanel
        addClientPanel.add(addClientLabel);
        addClientPanel.add(new JLabel("Navn:"));
        addClientPanel.add(clientNameTextField);
        addClientPanel.add(new JLabel("Inferno nr:"));
        addClientPanel.add(clientAccountNrTextField);
        addClientPanel.add(addClientButton);

        return addClientPanel;
    }

    private void clearTable(ViewModel viewModel, DefaultTableModel tableModel) {
        // clear trades table
        tableModel.setRowCount(0);
        viewModel.clearTrades();
        // clear files
        viewModel.clearFiles();
        filesLabel.setText("Ingen filer lastet opp enda.");
        outputArea.setText("");
        tradesTable = new JScrollPane(createTable());
    }

    private void showOutput(JTextArea area) {
        String output = viewModel.generateOutput();
        area.setText(output);
    }

    private JPanel newSecurityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        JTextField tickerArea = new JTextField();
        JTextField smidArea = new JTextField();
        JTextField isinArea = new JTextField();
        JButton addSecurityButton = new JButton("Legg til");

        addSecurityButton.addActionListener(e -> {
            String ticker = tickerArea.getText().trim();
            String isin = isinArea.getText().trim();
            int smid = Integer.parseInt(smidArea.getText().trim());

            viewModel.addSecurity(ticker, isin, smid);

            // update output
            showOutput(outputArea);

            // clear input fiels
            tickerArea.setText("");
            smidArea.setText("");
            isinArea.setText("");
        });

        JLabel title = new JLabel("Legg til nytt papir:");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(title);

        panel.add(new JLabel("Ticker:"));
        panel.add(tickerArea);

        panel.add(new JLabel("ISIN:"));
        panel.add(isinArea);

        panel.add(new JLabel("SMID:"));
        panel.add(smidArea);

        panel.add(addSecurityButton);

        return panel;
    }

    // pop-up window that lets user select path for client file
    private void selectFilePaths(ViewModel viewModel) {
        String cPath = JOptionPane.showInputDialog("Path to client file:");
        String sPath = JOptionPane.showInputDialog("Path to securities file:");

        // continue asking for path if file still not found
        while (viewModel.readClients(new File(cPath)) == null || viewModel.readSecurities(new File(sPath)) == null) {
            JOptionPane.showMessageDialog(null, "Could not find file on this path.");
            cPath = JOptionPane.showInputDialog("Path to client file:");
            sPath = JOptionPane.showInputDialog("Path to securities file:");
        }
    }
}

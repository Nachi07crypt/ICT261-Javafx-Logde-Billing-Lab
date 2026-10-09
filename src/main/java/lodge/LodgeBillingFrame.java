package lodge;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

/** The main window: menu bar, toolbar, input form, info panel and saved-bills table. */
public class LodgeBillingFrame extends JFrame {

    private final BillingService service;
    private final String username;
    private final Role role;
    private final Runnable logoutAction;

    // Input controls
    private final JTextField guestField = new JTextField(20);
    private final JComboBox<RoomType> roomCombo = new JComboBox<>(RoomType.values());
    private final JSpinner nightsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JTextField extrasField = new JTextField("0");
    private final JTextField paidField = new JTextField("0");
    private final JTextArea notesArea = new JTextArea(3, 20);

    // Buttons
    private final JButton calculateButton = new JButton("Calculate");
    private final JButton saveButton = new JButton("Save bill");

    // Output controls
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Bill", "Guest", "Room", "Nights", "Total", "Paid", "Balance"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public LodgeBillingFrame(BillingService service, String username, Role role, Runnable logoutAction) {
        super("Lodge Billing Lab - " + username);
        this.service = service;
        this.username = username;
        this.role = role;
        this.logoutAction = logoutAction;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setJMenuBar(buildMenuBar());

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.add(buildToolbar(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);

        wireEvents();
        refreshTable();   // show bills saved earlier this run
        setSize(940, 720);
        setLocationRelativeTo(null);
    }

    // ---------- building the screen ----------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setOpaque(true);
        bar.setBackground(Theme.BLACK);

        JMenu file = new JMenu("File");
        JMenuItem newGuest = new JMenuItem("New guest");
        newGuest.addActionListener(e -> onNewGuest());
        JMenuItem logout = new JMenuItem("Log out");
        logout.addActionListener(e -> onLogOut());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        file.add(newGuest);
        file.addSeparator();
        file.add(logout);
        file.add(exit);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Lodge Billing Lab\nICT261 Lecture 3\nICT group 102, Mulungushi University",
                "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        bar.add(file);
        bar.add(help);
        return bar;
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(true);
        bar.setBackground(Theme.BLACK);

        JButton newGuest = new JButton("New guest");
        newGuest.addActionListener(e -> onNewGuest());
        JButton receipt = new JButton("View receipt");
        receipt.addActionListener(e -> onViewReceipt());
        JButton delete = new JButton("Delete selected bill");
        delete.addActionListener(e -> onDeleteBill());

        bar.add(newGuest);
        bar.add(receipt);
        bar.add(delete);
        bar.add(Box.createHorizontalGlue());   // pushes the next items to the right
        JLabel who = new JLabel("Logged in as: " + username + " (" + role + ")   ");
        who.setForeground(Color.WHITE);
        bar.add(who);
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> onLogOut());
        bar.add(logout);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(4, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout(20, 0));
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildForm(), BorderLayout.CENTER);
        top.add(buildInfoPanel(), BorderLayout.EAST);
        top.add(buildButtonsAndStatus(), BorderLayout.SOUTH);

        content.add(top, BorderLayout.NORTH);
        content.add(buildTablePanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lodge Billing Lab");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("ICT261 Lecture 3   ICT group 102   Mulungushi University");
        subtitle.setForeground(Theme.ROYAL_PALE);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        addRow(form, 0, "Guest name", guestField);
        addRow(form, 1, "Room type", roomCombo);
        addRow(form, 2, "Nights", nightsSpinner);
        addRow(form, 3, "Extras in K", extrasField);
        addRow(form, 4, "Amount paid in K", paidField);
        addRow(form, 5, "Notes", new JScrollPane(notesArea));
        return form;
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(5, 0, 5, 14);
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(labelText), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private JPanel buildInfoPanel() {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setPreferredSize(new Dimension(260, 220));

        JLabel heading = new JLabel("Practice room prices");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        info.add(heading);
        info.add(Box.createVerticalStrut(6));

        // Built from the enum so the screen always matches the real rates
        for (RoomType type : RoomType.values()) {
            info.add(new JLabel(type + ": " + BillingService.money(type.getRatePerNight()) + " per night"));
        }
        info.add(Box.createVerticalStrut(18));
        info.add(noteLabel(BillingService.DISCOUNT_MIN_NIGHTS + " nights or more: 5% off the room charge."));
        info.add(Box.createVerticalStrut(6));
        info.add(noteLabel("Extras get no discount. No tax in this lab."));
        info.add(Box.createVerticalStrut(6));
        info.add(noteLabel("The amount paid must cover the total, or the bill is not saved."));
        info.add(Box.createVerticalStrut(6));
        info.add(noteLabel("Only administrators can delete bills."));
        info.add(Box.createVerticalStrut(6));
        info.add(noteLabel("Saved bills last only until you close the app."));
        return info;
    }

    private JLabel noteLabel(String text) {
        JLabel label = new JLabel("<html><body style='width:230px'>" + text + "</body></html>");
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel buildButtonsAndStatus() {
        Theme.styleAccentButton(saveButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.add(calculateButton);
        buttons.add(saveButton);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        JPanel south = new JPanel(new BorderLayout());
        south.add(buttons, BorderLayout.NORTH);
        south.add(statusLabel, BorderLayout.SOUTH);
        return south;
    }

    private JPanel buildTablePanel() {
        JLabel label = new JLabel("Saved bills (double-click a row to see its receipt)");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setBackground(Theme.ROYAL_PALE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Theme.ROYAL_DARK);
        table.setSelectionBackground(Theme.BLACK);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(Theme.BLACK);
        header.setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BLACK, 2));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void wireEvents() {
        calculateButton.addActionListener(e -> onCalculate());
        saveButton.addActionListener(e -> onSave());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onViewReceipt();
                }
            }
        });
    }

    // ---------- event handlers ----------

    private void onCalculate() {
        try {
            Calculation calc = service.calculate(
                    (RoomType) roomCombo.getSelectedItem(),
                    (Integer) nightsSpinner.getValue(),
                    readMoney(extrasField, "Extras"),
                    readMoney(paidField, "Amount paid"));
            String total = "Total: " + BillingService.money(calc.total());
            if (calc.balance().signum() > 0) {
                setStatus(total + "   Amount paid is not enough. Remaining: "
                        + BillingService.money(calc.balance()), true);
            } else if (calc.change().signum() > 0) {
                setStatus(total + "   Paid in full. Change due: "
                        + BillingService.money(calc.change()), false);
            } else {
                setStatus(total + "   Paid in full.", false);
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSave() {
        try {
            Bill bill = service.save(
                    guestField.getText(),
                    (RoomType) roomCombo.getSelectedItem(),
                    (Integer) nightsSpinner.getValue(),
                    readMoney(extrasField, "Extras"),
                    readMoney(paidField, "Amount paid"),
                    notesArea.getText(),
                    username);
            refreshTable();
            showReceipt(bill);

            String message = "Saved " + bill.id() + ". Balance: "
                    + BillingService.money(bill.calc().balance());
            if (bill.calc().change().signum() > 0) {
                message += "   Change given: " + BillingService.money(bill.calc().change());
            }
            onNewGuest();                    // clear the form for the next guest
            setStatus(message, false);
        } catch (InsufficientPaymentException ex) {
            setStatus("Amount not enough. Remaining: " + BillingService.money(ex.getRemaining()), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Payment not enough", JOptionPane.ERROR_MESSAGE);
            paidField.requestFocusInWindow();
            paidField.selectAll();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onNewGuest() {
        guestField.setText("");
        roomCombo.setSelectedIndex(0);
        nightsSpinner.setValue(1);
        extrasField.setText("0");
        paidField.setText("0");
        notesArea.setText("");
        setStatus(" ", false);
        guestField.requestFocusInWindow();
    }

    private void onViewReceipt() {
        Bill bill = selectedBill();
        if (bill == null) {
            showError("Select a bill in the table first.");
            return;
        }
        showReceipt(bill);
    }

    private void onDeleteBill() {
        if (role != Role.ADMIN) {
            JOptionPane.showMessageDialog(this,
                    "Only administrators can delete bills.\nLog out and use Admin log in to delete.",
                    "Access denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Bill bill = selectedBill();
        if (bill == null) {
            showError("Select a bill in the table first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete bill " + bill.id() + " for " + bill.guest() + "?",
                "Delete bill", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            service.delete(bill.id(), role);
            refreshTable();
            setStatus("Deleted " + bill.id() + ".", false);
        }
    }

    private void onLogOut() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Log out " + username + "?", "Log out", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            logoutAction.run();   // show the log in window first...
            dispose();            // ...then close this one
        }
    }

    // ---------- helpers ----------

    private void showReceipt(Bill bill) {
        JTextArea text = new JTextArea(bill.toReceipt());
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        text.setBackground(Color.WHITE);
        text.setForeground(Color.BLACK);
        text.setMargin(new Insets(8, 10, 8, 10));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(420, 380));
        JOptionPane.showMessageDialog(this, scroll, "Receipt " + bill.id(), JOptionPane.PLAIN_MESSAGE);
    }

    /** The bill for the highlighted table row, found by its ID. */
    private Bill selectedBill() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String id = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        return service.find(id);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Bill b : service.getBills()) {
            Calculation c = b.calc();
            tableModel.addRow(new Object[]{
                    b.id(), b.guest(), b.room(), b.nights(),
                    BillingService.money(c.total()),
                    BillingService.money(c.paid()),
                    BillingService.money(c.balance())});
        }
    }

    private void setStatus(String text, boolean warning) {
        statusLabel.setForeground(warning ? Theme.WARNING : Color.WHITE);
        statusLabel.setText(text);
    }

    /** Reads a money field. A blank field counts as 0. */
    private BigDecimal readMoney(JTextField field, String name) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a number, e.g. 200 or 150.50.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }
}

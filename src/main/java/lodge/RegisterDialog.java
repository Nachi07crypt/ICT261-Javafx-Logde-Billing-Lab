package lodge;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * The "Create account" window. Opens on top of the log in window and blocks it
 * until it is closed. showDialog() returns the new username, or null if the
 * person cancelled.
 */
public class RegisterDialog extends JDialog {

    private final AuthService auth;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton createButton = new JButton("Create account");
    private final JButton cancelButton = new JButton("Cancel");

    private String createdUsername = null;

    public RegisterDialog(JFrame owner, AuthService auth) {
        super(owner, "Lodge Billing Lab - Create account", true);   // true = modal
        this.auth = auth;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        getRootPane().setDefaultButton(createButton);   // Enter key = Create account
        createButton.addActionListener(e -> onCreate());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Shows the window and waits. Returns the new username, or null if cancelled. */
    public String showDialog() {
        setVisible(true);
        return createdUsername;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Create an account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel hint = new JLabel("<html><body style='width:260px'>Username: 3 to 20 letters, numbers "
                + "or underscores.<br>Password: at least 6 characters.</body></html>");
        hint.setForeground(Theme.ROYAL_PALE);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(hint);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        addRow(form, 0, "Username", usernameField);
        addRow(form, 1, "Password", passwordField);
        addRow(form, 2, "Confirm password", confirmField);
        return form;
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(5, 0, 5, 12);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(labelText), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private JPanel buildFooter() {
        messageLabel.setForeground(Theme.WARNING);
        Theme.styleAccentButton(createButton);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.add(createButton);
        buttonRow.add(cancelButton);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.add(buttonRow, BorderLayout.NORTH);
        footer.add(messageLabel, BorderLayout.SOUTH);
        return footer;
    }

    private void onCreate() {
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();
        try {
            if (!Arrays.equals(password, confirm)) {
                throw new IllegalArgumentException("The two passwords do not match.");
            }
            auth.register(usernameField.getText(), password);   // checks the rules, saves the account
            createdUsername = usernameField.getText().trim();
            dispose();
        } catch (IllegalArgumentException ex) {
            messageLabel.setText(ex.getMessage());
            passwordField.setText("");
            confirmField.setText("");
            passwordField.requestFocusInWindow();
        } finally {
            Arrays.fill(password, ' ');   // wipe the passwords from memory
            Arrays.fill(confirm, ' ');
        }
    }
}

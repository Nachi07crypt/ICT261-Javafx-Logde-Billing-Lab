package lodge;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * The log in window. "Log in" is for guest, "Admin log in" is for administrators.
 * Calls onSuccess with the username and role when the details are right.
 */
public class LoginFrame extends JFrame {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_SECONDS = 10;

    private final AuthService auth;
    private final BiConsumer<String, Role> onSuccess;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JButton loginButton = new JButton("Guest log in");
    private final JButton adminButton = new JButton("Admin log in");
    private final JButton registerButton = new JButton("Create account");
    private final JLabel messageLabel = new JLabel(" ");

    private int failedAttempts = 0;

    public LoginFrame(AuthService auth, BiConsumer<String, Role> onSuccess) {
        super("Lodge Billing Lab - Log in");
        this.auth = auth;
        this.onSuccess = onSuccess;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        getRootPane().setDefaultButton(loginButton);   // Enter key = Log in
        loginButton.addActionListener(e -> attemptLogin(false));
        adminButton.addActionListener(e -> attemptLogin(true));
        registerButton.addActionListener(e -> openRegister());

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lodge Billing Lab");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Please log in to continue");
        subtitle.setForeground(Theme.ROYAL_PALE);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        addRow(form, 0, "Username", usernameField);
        addRow(form, 1, "Password", passwordField);
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
        Theme.styleAccentButton(loginButton);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.add(loginButton);
        buttonRow.add(adminButton);
        buttonRow.add(registerButton);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.add(buttonRow, BorderLayout.NORTH);
        footer.add(messageLabel, BorderLayout.SOUTH);
        return footer;
    }

    /** adminOnly = true when the Admin log in button was pressed. */
    private void attemptLogin(boolean adminOnly) {
        char[] password = passwordField.getPassword();
        Optional<Role> role = auth.authenticate(usernameField.getText(), password);
        Arrays.fill(password, ' ');          // wipe the password from memory
        passwordField.setText("");
        messageLabel.setForeground(Theme.WARNING);

        if (role.isEmpty()) {
            registerFailure("Wrong username or password.");
            return;
        }
        if (adminOnly && role.get() != Role.ADMIN) {
            registerFailure("That account is not an administrator.");
            return;
        }
        if (!adminOnly && role.get() == Role.ADMIN) {
            messageLabel.setText("Administrators must use the Admin log in button.");
            passwordField.requestFocusInWindow();
            return;
        }

        String username = usernameField.getText().trim();
        onSuccess.accept(username, role.get());   // open the main window first...
        dispose();                                // ...then close this one
    }

    private void registerFailure(String reason) {
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
            startLockout();
        } else {
            messageLabel.setText(reason + " Attempt " + failedAttempts + " of " + MAX_ATTEMPTS + ".");
            passwordField.requestFocusInWindow();
        }
    }

    /** Opens the Create account window. If an account is made, fill in the username. */
    private void openRegister() {
        String created = new RegisterDialog(this, auth).showDialog();
        if (created != null) {
            failedAttempts = 0;
            usernameField.setText(created);
            passwordField.setText("");
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setText("Account created. Please log in.");
            passwordField.requestFocusInWindow();
        }
    }

    /** After too many wrong attempts, disable log in for a few seconds. */
    private void startLockout() {
        setLoginControlsEnabled(false);
        int[] secondsLeft = {LOCKOUT_SECONDS};
        messageLabel.setText("Too many attempts. Try again in " + secondsLeft[0] + "s.");

        Timer timer = new Timer(1000, null);
        timer.addActionListener(e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) {
                messageLabel.setText("Too many attempts. Try again in " + secondsLeft[0] + "s.");
            } else {
                timer.stop();
                failedAttempts = 0;
                setLoginControlsEnabled(true);
                messageLabel.setText(" ");
                passwordField.requestFocusInWindow();
            }
        });
        timer.start();
    }

    private void setLoginControlsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        adminButton.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }
}

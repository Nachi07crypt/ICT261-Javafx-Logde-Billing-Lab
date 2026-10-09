package lodge;

import javax.swing.SwingUtilities;

/**
 * Starts the app at the log in screen. One AuthService and one BillingService
 * are shared for the whole run, so saved bills survive a log out and the next
 * person who logs in sees them. They are lost only when the app closes.
 */
public class Main {

    private static final AuthService AUTH = new AuthService();
    private static final BillingService BILLING = new BillingService();

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(Main::showLogin);
    }

    private static void showLogin() {
        new LoginFrame(AUTH, Main::showBilling).setVisible(true);
    }

    private static void showBilling(String username, Role role) {
        new LodgeBillingFrame(BILLING, username, role, Main::showLogin).setVisible(true);
    }
}

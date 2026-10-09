package lodge;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;

/**
 * All the colours of the app in one place: royal blue backgrounds with
 * hints of black (menu bar, toolbar, table header, selections, key buttons).
 * Change the constants below to restyle the whole app.
 */
public final class Theme {

    public static final Color ROYAL_BLUE = new Color(0x4169E1);
    public static final Color ROYAL_DARK = new Color(0x2A4BB8);
    public static final Color ROYAL_PALE = new Color(0xE6ECFF);   // table rows, soft text
    public static final Color BLACK = new Color(0x0D0D12);        // the "hints of black"
    public static final Color WARNING = new Color(0xFFD166);      // readable on royal blue

    private Theme() {
    }

    /** Call once, before any window is created. */
    public static void install() {
        MetalLookAndFeel.setCurrentTheme(new RoyalTheme());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ignored) {
            // keep the default look and feel
        }
        UIManager.put("Panel.background", ROYAL_BLUE);
        UIManager.put("OptionPane.background", ROYAL_BLUE);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        // Input boxes stay white with black text so they are easy to read
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("ComboBox.selectionBackground", BLACK);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("PasswordField.foreground", Color.BLACK);
        UIManager.put("TextArea.foreground", Color.BLACK);
        UIManager.put("FormattedTextField.foreground", Color.BLACK);
    }

    /** A black button with a white edge, used for the main action on a screen. */
    public static void styleAccentButton(JButton button) {
        button.setBackground(BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
    }

    /** Royal blue panels, white text, black borders and menus. */
    private static final class RoyalTheme extends DefaultMetalTheme {
        private static final ColorUIResource P1 = new ColorUIResource(BLACK);
        private static final ColorUIResource P2 = new ColorUIResource(ROYAL_DARK);
        private static final ColorUIResource P3 = new ColorUIResource(BLACK);        // selections
        private static final ColorUIResource S1 = new ColorUIResource(BLACK);        // dark borders
        private static final ColorUIResource S2 = new ColorUIResource(ROYAL_DARK);   // shadows
        private static final ColorUIResource S3 = new ColorUIResource(ROYAL_BLUE);   // panel colour
        private static final ColorUIResource WHITE = new ColorUIResource(Color.WHITE);

        @Override
        public String getName() {
            return "Royal Blue";
        }

        @Override
        protected ColorUIResource getPrimary1() {
            return P1;
        }

        @Override
        protected ColorUIResource getPrimary2() {
            return P2;
        }

        @Override
        protected ColorUIResource getPrimary3() {
            return P3;
        }

        @Override
        protected ColorUIResource getSecondary1() {
            return S1;
        }

        @Override
        protected ColorUIResource getSecondary2() {
            return S2;
        }

        @Override
        protected ColorUIResource getSecondary3() {
            return S3;
        }

        @Override
        public ColorUIResource getControlTextColor() {
            return WHITE;                                  // labels and buttons: white text
        }

        @Override
        public ColorUIResource getFocusColor() {
            return WHITE;
        }

        @Override
        public ColorUIResource getMenuBackground() {
            return new ColorUIResource(BLACK);
        }

        @Override
        public ColorUIResource getMenuForeground() {
            return WHITE;
        }

        @Override
        public ColorUIResource getMenuSelectedBackground() {
            return new ColorUIResource(ROYAL_BLUE);
        }

        @Override
        public ColorUIResource getMenuSelectedForeground() {
            return WHITE;
        }
    }
}

package lodge;

import java.math.BigDecimal;

/** The three practice room types and their nightly rates (in Kwacha). */
public enum RoomType {
    STANDARD("Standard", "350.00"),
    DELUXE("Deluxe", "550.00"),
    FAMILY("Family", "750.00");

    private final String label;
    private final BigDecimal ratePerNight;

    RoomType(String label, String rate) {
        this.label = label;
        this.ratePerNight = new BigDecimal(rate);
    }

    public BigDecimal getRatePerNight() {
        return ratePerNight;
    }

    /** Shown in the combo box and the table. */
    @Override
    public String toString() {
        return label;
    }
}

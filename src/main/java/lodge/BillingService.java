package lodge;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic: works out bills and keeps the saved bills in memory.
 *
 * Data structure: a LinkedHashMap keyed by bill ID. Looking up or deleting a
 * bill by its ID is fast, and bills stay in the order they were saved.
 * There is no database, so everything is lost when the app closes.
 */
public class BillingService {

    public static final int DISCOUNT_MIN_NIGHTS = 5;
    public static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    private final Map<String, Bill> bills = new LinkedHashMap<>();
    private int nextNumber = 1;

    /** Formats an amount the way the lab does, e.g. "K 2812.50". */
    public static String money(BigDecimal amount) {
        return "K " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public Calculation calculate(RoomType room, int nights, BigDecimal extras, BigDecimal paid) {
        if (room == null) {
            throw new IllegalArgumentException("Please choose a room type.");
        }
        if (nights < 1) {
            throw new IllegalArgumentException("Nights must be at least 1.");
        }
        if (extras.signum() < 0 || paid.signum() < 0) {
            throw new IllegalArgumentException("Extras and amount paid cannot be negative.");
        }

        BigDecimal gross = room.getRatePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (nights >= DISCOUNT_MIN_NIGHTS) {
            discount = gross.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal roomCharge = gross.subtract(discount);
        BigDecimal extrasScaled = extras.setScale(2, RoundingMode.HALF_UP);
        BigDecimal paidScaled = paid.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = roomCharge.add(extrasScaled);       // extras: no discount, no tax

        BigDecimal difference = total.subtract(paidScaled);
        BigDecimal balance = difference.signum() > 0 ? difference : BigDecimal.ZERO.setScale(2);
        BigDecimal change = difference.signum() < 0 ? difference.negate() : BigDecimal.ZERO.setScale(2);

        return new Calculation(gross, discount, roomCharge, extrasScaled, total, paidScaled, balance, change);
    }

    /**
     * Saves a bill once the guest has paid in full.
     *
     * @throws InsufficientPaymentException if the amount paid is less than the total
     */
    public Bill save(String guest, RoomType room, int nights, BigDecimal extras,
                     BigDecimal paid, String notes, String servedBy) {
        if (guest == null || guest.isBlank()) {
            throw new IllegalArgumentException("Please enter the guest name.");
        }
        Calculation calc = calculate(room, nights, extras, paid);
        if (calc.balance().signum() > 0) {
            throw new InsufficientPaymentException(calc.balance());
        }
        String id = String.format("B%03d", nextNumber++);
        Bill bill = new Bill(id, guest.trim(), room, nights, notes, servedBy, LocalDateTime.now(), calc);
        bills.put(id, bill);
        return bill;
    }

    public Bill find(String billId) {
        return bills.get(billId);
    }

    /**
     * Deletes a bill. Only administrators are allowed to.
     *
     * @throws SecurityException if the requester is not an admin
     */
    public boolean delete(String billId, Role requester) {
        if (requester != Role.ADMIN) {
            throw new SecurityException("Only administrators can delete bills.");
        }
        return bills.remove(billId) != null;
    }

    /** A snapshot of the saved bills, oldest first. */
    public List<Bill> getBills() {
        return List.copyOf(bills.values());
    }
}

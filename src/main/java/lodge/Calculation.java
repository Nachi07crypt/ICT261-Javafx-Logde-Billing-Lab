package lodge;

import java.math.BigDecimal;

/**
 * The result of working out a bill. All amounts are in Kwacha with 2 decimal places.
 * balance = what is still owed (never negative); change = what the guest overpaid.
 */
public record Calculation(
        BigDecimal grossRoomCharge,
        BigDecimal discount,
        BigDecimal roomCharge,
        BigDecimal extras,
        BigDecimal total,
        BigDecimal paid,
        BigDecimal balance,
        BigDecimal change) {
}

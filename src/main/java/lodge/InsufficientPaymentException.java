package lodge;

import java.math.BigDecimal;

/** Thrown when the amount paid does not cover the total. Carries the amount still owed. */
public class InsufficientPaymentException extends IllegalArgumentException {

    private final BigDecimal remaining;

    public InsufficientPaymentException(BigDecimal remaining) {
        super("The amount paid is not enough.\nRemaining amount: " + BillingService.money(remaining));
        this.remaining = remaining;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }
}

package lodge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A saved bill. */
public record Bill(
        String id,
        String guest,
        RoomType room,
        int nights,
        String notes,
        String servedBy,
        LocalDateTime issued,
        Calculation calc) {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public String toReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("LODGE BILLING LAB - RECEIPT\n");
        sb.append("ICT group 102, Mulungushi University\n");
        sb.append("==========================================\n");
        sb.append(String.format("Bill no.   : %s%n", id));
        sb.append(String.format("Date       : %s%n", issued.format(WHEN)));
        sb.append(String.format("Served by  : %s%n", servedBy));
        sb.append(String.format("Guest      : %s%n", guest));
        sb.append(String.format("Room type  : %s%n", room));
        sb.append(String.format("Nights     : %d%n", nights));
        sb.append("------------------------------------------\n");
        sb.append(String.format("Room (%d x %s) : %s%n", nights,
                BillingService.money(room.getRatePerNight()), BillingService.money(calc.grossRoomCharge())));
        if (calc.discount().signum() > 0) {
            sb.append(String.format("Discount 5%%    : -%s%n", BillingService.money(calc.discount())));
        }
        sb.append(String.format("Extras         : %s%n", BillingService.money(calc.extras())));
        sb.append(String.format("TOTAL          : %s%n", BillingService.money(calc.total())));
        sb.append(String.format("Paid           : %s%n", BillingService.money(calc.paid())));
        sb.append(String.format("BALANCE        : %s%n", BillingService.money(calc.balance())));
        if (calc.change().signum() > 0) {
            sb.append(String.format("CHANGE DUE     : %s%n", BillingService.money(calc.change())));
        }
        if (notes != null && !notes.isBlank()) {
            sb.append("------------------------------------------\n");
            sb.append("Notes: ").append(notes.trim()).append('\n');
        }
        sb.append("==========================================\n");
        sb.append("Thank you for staying with us.\n");
        return sb.toString();
    }
}

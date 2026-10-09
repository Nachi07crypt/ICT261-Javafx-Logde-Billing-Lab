package lodge;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private final BillingService service = new BillingService();

    @Test
    void deluxeFiveNightsGetsDiscount() {
        // 5 x 550 = 2750, less 5% = 2612.50, plus 200 extras = 2812.50
        Calculation c = service.calculate(RoomType.DELUXE, 5,
                new BigDecimal("200"), new BigDecimal("1000"));
        assertEquals(new BigDecimal("2812.50"), c.total());
        assertEquals(new BigDecimal("1812.50"), c.balance());
    }

    @Test
    void standardTwoNightsNoDiscount() {
        // 2 x 350 = 700 + 100 extras = 800, paid 500, still owes 300
        Calculation c = service.calculate(RoomType.STANDARD, 2,
                new BigDecimal("100"), new BigDecimal("500"));
        assertEquals(new BigDecimal("800.00"), c.total());
        assertEquals(new BigDecimal("300.00"), c.balance());
    }

    @Test
    void fourNightsHasNoDiscount() {
        Calculation c = service.calculate(RoomType.FAMILY, 4, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("3000.00"), c.total());
    }

    @Test
    void payingTooLittleIsRejectedAndShowsRemainingAmount() {
        InsufficientPaymentException ex = assertThrows(InsufficientPaymentException.class, () ->
                service.save("Mary Banda", RoomType.STANDARD, 2,
                        new BigDecimal("100"), new BigDecimal("500"), "", "admin"));
        assertEquals(new BigDecimal("300.00"), ex.getRemaining());
        assertTrue(ex.getMessage().contains("K 300.00"));
        assertTrue(service.getBills().isEmpty());      // nothing was saved
    }

    @Test
    void payingExactlyTheTotalSaves() {
        Bill bill = service.save("John Phiri", RoomType.DELUXE, 5,
                new BigDecimal("200"), new BigDecimal("2812.50"), "", "admin");
        assertEquals(new BigDecimal("0.00"), bill.calc().balance());
        assertEquals(new BigDecimal("0.00"), bill.calc().change());
    }

    @Test
    void overpayingGivesChange() {
        Bill bill = service.save("John Phiri", RoomType.STANDARD, 1,
                BigDecimal.ZERO, new BigDecimal("400"), "", "admin");
        assertEquals(new BigDecimal("50.00"), bill.calc().change());
        assertEquals(new BigDecimal("0.00"), bill.calc().balance());
    }

    @Test
    void billIdsCountUpAndDoNotRepeatAfterDelete() {
        Bill first = service.save("Mary Banda", RoomType.STANDARD, 1,
                BigDecimal.ZERO, new BigDecimal("350"), "", "admin");
        assertTrue(service.delete(first.id(), Role.ADMIN));
        Bill second = service.save("John Phiri", RoomType.STANDARD, 1,
                BigDecimal.ZERO, new BigDecimal("350"), "", "admin");
        assertEquals("B001", first.id());
        assertEquals("B002", second.id());
        assertNull(service.find("B001"));
        assertEquals(1, service.getBills().size());
    }

    @Test
    void onlyAdminsCanDeleteBills() {
        Bill bill = service.save("John Phiri", RoomType.STANDARD, 1,
                BigDecimal.ZERO, new BigDecimal("350"), "", "reception");
        assertThrows(SecurityException.class, () -> service.delete(bill.id(), Role.GUEST));
        assertNotNull(service.find(bill.id()));            // still there
        assertTrue(service.delete(bill.id(), Role.ADMIN));
        assertNull(service.find(bill.id()));
    }

    @Test
    void receiptShowsServedByAndTotals() {
        Bill bill = service.save("John Phiri", RoomType.DELUXE, 5,
                new BigDecimal("200"), new BigDecimal("3000"), "", "reception");
        String receipt = bill.toReceipt();
        assertTrue(receipt.contains("reception"));
        assertTrue(receipt.contains("K 2812.50"));
        assertTrue(receipt.contains("CHANGE DUE"));
    }

    @Test
    void blankGuestNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.save(" ", RoomType.STANDARD, 1, BigDecimal.ZERO, new BigDecimal("350"), "", "admin"));
    }
}

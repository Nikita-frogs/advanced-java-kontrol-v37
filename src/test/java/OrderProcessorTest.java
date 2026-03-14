import domain.*;
import domain.Order;
import exceptions.*;
import org.junit.jupiter.api.*;
import service.*;
import service.OrderRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private OrderProcessorTemplate processor;
    private OrderRepository mockRepo;
    private Order validOrder;

    @BeforeEach
    void setUp() {
        OrderItem[] items = {
                new OrderItem("Laptop", 1, new Money(new BigDecimal("10000")))
        };
        validOrder = new Order("ORD-1", new Email("test@mail.com"), items);

        mockRepo = id -> {
            if (id.equals(validOrder.getId())) return Optional.of(validOrder);
            if (id.equals("FAIL_CONFIRM")) return Optional.of(new Order("FAIL_CONFIRM", new Email("a@b.c"), items));
            return Optional.empty();
        };

        processor = new StandardOrderProcessor(mockRepo);
    }

    @Test
    void testSuccessfulProcessingFlow() {
        processor.process("ORD-1", new BankTransferPayment());
        assertEquals(OrderStatus.DELIVERED, validOrder.getStatus());
        assertEquals(new BigDecimal("10000.00"), validOrder.getTotalAmount().getAmount().setScale(2));
    }

    @Test
    void testVipDiscountCalculation() {
        OrderItem[] items = { new OrderItem("PC", 1, new Money(new BigDecimal("16000"))) };
        Order vipOrder = new Order("VIP-1", new Email("vip@mail.com"), items, true);

        OrderRepository repo = id -> Optional.of(vipOrder);
        StandardOrderProcessor vipProcessor = new StandardOrderProcessor(repo);

        vipProcessor.process("VIP-1", new BankTransferPayment());

        assertEquals(new BigDecimal("14400.00"), vipOrder.getTotalAmount().getAmount().setScale(2));
    }

    @Test
    void testNonVipDiscountCalculation() {
        OrderItem[] items = { new OrderItem("PC", 1, new Money(new BigDecimal("16000"))) };
        Order ord = new Order("ORD-2", new Email("a@a.com"), items, false);

        StandardOrderProcessor proc = new StandardOrderProcessor(id -> Optional.of(ord));
        proc.process("ORD-2", new BankTransferPayment());

        assertEquals(new BigDecimal("14720.00"), ord.getTotalAmount().getAmount().setScale(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"200", "5000", "19999"})
    void testPayPalValidAmounts(String amount) {
        PaymentMethod payPal = new PayPalPayment();
        assertDoesNotThrow(() -> payPal.pay(new Money(new BigDecimal(amount))));
    }

    @Test
    void testNegative1_ValidationTooManyItems() {
        OrderItem item = new OrderItem("A", 1, new Money(new BigDecimal("10")));
        OrderItem[] items = {item, item, item, item, item, item}; // 6 items
        Order order = new Order("ERR-1", new Email("a@b.c"), items);

        StandardOrderProcessor proc = new StandardOrderProcessor(id -> Optional.of(order));
        assertThrows(ValidationException.class, () -> proc.process("ERR-1", new CardPayment()));
    }

    @Test
    void testNegative2_ValidationQuantityTooHigh() {
        OrderItem[] items = { new OrderItem("A", 25, new Money(new BigDecimal("10"))) }; // 25 > 20
        Order order = new Order("ERR-2", new Email("a@b.c"), items);

        StandardOrderProcessor proc = new StandardOrderProcessor(id -> Optional.of(order));
        assertThrows(ValidationException.class, () -> proc.process("ERR-2", new CardPayment()));
    }

    @Test
    void testNegative3_CardLimitExceeded() {
        OrderItem[] items = { new OrderItem("Server", 1, new Money(new BigDecimal("25000"))) };
        Order order = new Order("ERR-3", new Email("a@b.c"), items);

        StandardOrderProcessor proc = new StandardOrderProcessor(id -> Optional.of(order));
        assertThrows(PaymentException.class, () -> proc.process("ERR-3", new CardPayment()));
    }

    @Test
    void testNegative4_PayPalMinimumLimit() {
        OrderItem[] items = { new OrderItem("Cable", 1, new Money(new BigDecimal("50"))) };
        Order order = new Order("ERR-4", new Email("a@b.c"), items);

        StandardOrderProcessor proc = new StandardOrderProcessor(id -> Optional.of(order));
        assertThrows(PaymentException.class, () -> proc.process("ERR-4", new PayPalPayment()));
    }

    @Test
    void testNegative5_ShipmentConfirmationException() {
        AppException exception = assertThrows(AppException.class,
                () -> processor.process("FAIL_CONFIRM", new BankTransferPayment()));

        assertTrue(exception.getCause() instanceof ShipmentConfirmationException);
    }

    @Test
    void testOrderNotFound() {
        assertThrows(AppException.class, () -> processor.process("UNKNOWN_ID", new CardPayment()));
    }
}

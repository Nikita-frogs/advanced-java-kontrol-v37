package service;

import domain.Money;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankTransferPayment implements PaymentMethod {
    private static final Logger log = LoggerFactory.getLogger(BankTransferPayment.class);

    @Override
    public void pay(Money amount) {
        BigDecimal fee = amount.getAmount().multiply(new BigDecimal("0.01"));
        log.info("Bank transfer initiated. Commission (1%): {}", fee);
    }
}

package service;

import domain.*;
import exceptions.*;

import java.math.BigDecimal;

public class CardPayment implements PaymentMethod {
    @Override
    public void pay(Money amount) {
        if (amount.getAmount().compareTo(new BigDecimal("20000")) > 0) {
            throw new PaymentException("Card limit exceeded (max 20_000).");
        }
    }
}

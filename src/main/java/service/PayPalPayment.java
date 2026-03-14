package service;

import domain.*;
import exceptions.*;

import java.math.BigDecimal;

public class PayPalPayment implements PaymentMethod {
    @Override
    public void pay(Money amount) {
        if (amount.getAmount().compareTo(new BigDecimal("100")) < 0) {
            throw new PaymentException("PayPal min amount is 100.");
        }
    }
}

package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizerSelectorTest {

    private AuthorizerSelector selector;

    @BeforeEach
    void setUp() {
        selector = new AuthorizerSelector(java.util.List.of(
            new AutomaticAuthorizer(BigDecimal.valueOf(10000)),
            new ManualAuthorizer(BigDecimal.valueOf(100000), BigDecimal.valueOf(10000)),
            new SupervisorAuthorizer(BigDecimal.valueOf(100000))
        ));
    }

    @Test
    void select_lowAmountPLN_returnsAutomatic() {
        Transaction tx = createTransaction("1000", "PLN");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.AUTOMATIC, authorizer.getType());
    }

    @Test
    void select_mediumAmountPLN_returnsManual() {
        Transaction tx = createTransaction("50000", "PLN");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.MANUAL, authorizer.getType());
    }

    @Test
    void select_highAmountPLN_returnsSupervisor() {
        Transaction tx = createTransaction("150000", "PLN");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.SUPERVISOR, authorizer.getType());
    }

    @Test
    void select_foreignCurrency_returnsSupervisor() {
        Transaction tx = createTransaction("1000", "EUR");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.SUPERVISOR, authorizer.getType());
    }

    @Test
    void select_boundaryAutomatic_returnsAutomatic() {
        Transaction tx = createTransaction("9999", "PLN");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.AUTOMATIC, authorizer.getType());
    }

    @Test
    void select_boundaryManual_returnsManual() {
        Transaction tx = createTransaction("10000", "PLN");
        Authorizer authorizer = selector.select(tx);
        assertEquals(Authorizer.AuthorizerType.MANUAL, authorizer.getType());
    }

    private Transaction createTransaction(String amount, String currency) {
        Transaction tx = new Transaction();
        tx.setAmount(new BigDecimal(amount));
        tx.setCurrency(currency);
        tx.setValueDate(LocalDate.now().plusDays(1));
        tx.setSenderAccount("PL61109010140000071219812874");
        tx.setReceiverAccount("PL61109010140000071219812874");
        tx.setStatus(TransactionStatus.VALIDATED);
        return tx;
    }
}

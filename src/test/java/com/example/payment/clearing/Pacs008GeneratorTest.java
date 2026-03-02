package com.example.payment.clearing;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Pacs008GeneratorTest {

    private final Pacs008Generator generator = new Pacs008Generator();

    @Test
    void generatePacs008_createsValidXml() {
        Transaction tx = createTransaction(1L, "TX001", BigDecimal.valueOf(1000), "PLN");
        
        String xml = generator.generatePacs008(List.of(tx));
        
        assertNotNull(xml);
        assertTrue(xml.contains("<?xml version"));
        assertTrue(xml.contains("pacs.008"));
        assertTrue(xml.contains("FIToFICstmrCdtTrf"));
    }

    @Test
    void generatePacs008_containsTransactionData() {
        Transaction tx = createTransaction(1L, "TX001", BigDecimal.valueOf(1000.50), "PLN");
        
        String xml = generator.generatePacs008(List.of(tx));
        
        assertTrue(xml.contains("TX001"));
        assertTrue(xml.contains("1000.5") || xml.contains("1000.50"));
        assertTrue(xml.contains("PLN"));
    }

    @Test
    void generatePacs008_multipleTransactions() {
        Transaction tx1 = createTransaction(1L, "TX001", BigDecimal.valueOf(100), "PLN");
        Transaction tx2 = createTransaction(2L, "TX002", BigDecimal.valueOf(200), "EUR");
        
        String xml = generator.generatePacs008(List.of(tx1, tx2));
        
        assertTrue(xml.contains("TX001"));
        assertTrue(xml.contains("TX002"));
        assertTrue(xml.contains("100"));
        assertTrue(xml.contains("200"));
    }

    @Test
    void generatePacs008_emptyList_returnsValidXml() {
        String xml = generator.generatePacs008(List.of());
        
        assertNotNull(xml);
        assertTrue(xml.contains("NbOfTxs>0"));
    }

    @Test
    void generatePacs008_escapesXmlSpecialCharacters() {
        Transaction tx = createTransaction(1L, "TX001", BigDecimal.valueOf(100), "PLN");
        tx.setPaymentTitle("<test>&\"'");
        
        String xml = generator.generatePacs008(List.of(tx));
        
        assertTrue(xml.contains("&lt;test&gt;"));
        assertFalse(xml.contains("<test>"));
    }

    private Transaction createTransaction(Long id, String txId, BigDecimal amount, String currency) {
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setTransactionId(txId);
        tx.setPainMessageId("MSG" + id);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setValueDate(LocalDate.now().plusDays(1));
        tx.setSenderName("John Doe");
        tx.setSenderAccount("PL61109010140000071219812874");
        tx.setReceiverName("Jane Smith");
        tx.setReceiverAccount("PL61109010140000071219812875");
        tx.setPaymentTitle("Test payment");
        tx.setStatus(TransactionStatus.AUTHORIZED);
        return tx;
    }
}

package com.example.payment.clearing;

import com.example.payment.domain.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class Pacs008Generator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String generatePacs008(List<Transaction> transactions) {
        StringBuilder xml = new StringBuilder();
        
        String messageId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10\" ");
        xml.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        xml.append("  <FIToFICstmrCdtTrf>\n");
        
        // GrpHdr - Group Header
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>").append(escapeXml(messageId)).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(now.format(DATE_TIME_FORMATTER)).append("</CreDtTm>\n");
        xml.append("      <NbOfTxs>").append(transactions.size()).append("</NbOfTxs>\n");
        xml.append("      <CtrlSum>").append(calculateTotal(transactions)).append("</CtrlSum>\n");
        xml.append("      <InitgPty>\n");
        xml.append("        <Nm>PaymentProcessor</Nm>\n");
        xml.append("      </InitgPty>\n");
        xml.append("    </GrpHdr>\n");
        
        // CdtTrfTxInf for each transaction
        for (Transaction tx : transactions) {
            xml.append("    <CdtTrfTxInf>\n");
            
            // PaymentIdentification
            xml.append("      <PmtId>\n");
            xml.append("        <EndToEndId>").append(escapeXml(tx.getTransactionId())).append("</EndToEndId>\n");
            xml.append("        <TxId>").append(tx.getId()).append("</TxId>\n");
            xml.append("      </PmtId>\n");
            
            // InterbankSettlementAmount
            xml.append("      <IntrBkSttlmAmt Ccy=\"").append(tx.getCurrency()).append("\">");
            xml.append(tx.getAmount().toPlainString());
            xml.append("</IntrBkSttlmAmt>\n");
            
            // InterbankSettlementDate
            xml.append("      <IntrbkSttlmDt>").append(tx.getValueDate().format(DATE_FORMATTER)).append("</IntrbkSttlmDt>\n");
            
            // Debtor (sender)
            xml.append("      <Dbtr>\n");
            xml.append("        <Nm>").append(escapeXml(tx.getSenderName() != null ? tx.getSenderName() : "")).append("</Nm>\n");
            xml.append("      </Dbtr>\n");
            
            // DebtorAccount (sender IBAN)
            xml.append("      <DbtrAcct>\n");
            xml.append("        <Id>\n");
            xml.append("          <IBAN>").append(escapeXml(tx.getSenderAccount())).append("</IBAN>\n");
            xml.append("        </Id>\n");
            xml.append("      </DbtrAcct>\n");
            
            // Creditor (receiver)
            xml.append("      <Cdtr>\n");
            xml.append("        <Nm>").append(escapeXml(tx.getReceiverName() != null ? tx.getReceiverName() : "")).append("</Nm>\n");
            xml.append("      </Cdtr>\n");
            
            // CreditorAccount (receiver IBAN)
            xml.append("      <CdtrAcct>\n");
            xml.append("        <Id>\n");
            xml.append("          <IBAN>").append(escapeXml(tx.getReceiverAccount())).append("</IBAN>\n");
            xml.append("        </Id>\n");
            xml.append("      </CdtrAcct>\n");
            
            // RemittanceInformation
            xml.append("      <RmtInf>\n");
            xml.append("        <Ustrd>").append(escapeXml(tx.getPaymentTitle() != null ? tx.getPaymentTitle() : "")).append("</Ustrd>\n");
            xml.append("      </RmtInf>\n");
            
            xml.append("    </CdtTrfTxInf>\n");
        }
        
        xml.append("  </FIToFICstmrCdtTrf>\n");
        xml.append("</Document>\n");
        
        return xml.toString();
    }

    private String calculateTotal(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .toPlainString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

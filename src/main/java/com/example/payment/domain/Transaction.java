package com.example.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ISO20022 Group Header (wspólne dla pain.001 i pacs.008)
    @Column(name = "message_id", length = 35)
    private String messageId;  // MsgId
    
    @Column(name = "message_type", length = 10)
    private String messageType;  // pain.001 lub pacs.008
    
    @Column(name = "creation_date_time")
    private Instant creationDateTime;  // CreDtTm
    
    @Column(name = "number_of_transactions")
    private Integer numberOfTransactions;  // NbOfTxs
    
    @Column(name = "control_sum", precision = 19, scale = 4)
    private BigDecimal controlSum;  // CtrlSum

    // Pain.001 - Payment Instruction
    @Column(name = "payment_instruction_id", length = 35)
    private String paymentInstructionId;  // PmtInfId
    
    @Column(name = "payment_method", length = 3)
    private String paymentMethod;  // PmtMtd (TRN, TRA, CCP)
    
    @Column(name = "batch_booking")
    private Boolean batchBooking;  // BtchBookg
    
    @Column(name = "requested_execution_date")
    private LocalDate requestedExecutionDate;  // ReqdExctnDt
    
    @Column(name = "chargeBearer", length = 4)
    private String chargeBearer;  // ChrgBr (SLEV, SHAR, DEBT, CRCD)

    // Debtor (Nadawca)
    @Column(name = "debtor_name")
    private String debtorName;  // Dbtr -> Nm
    
    @Column(name = "debtor_legal_name")
    private String debtorLegalName;  // Dbtr -> Id -> OrgId -> LEI
    
    @Column(name = "debtor_account_iban")
    private String debtorAccountIban;  // DbtrAcct -> Id -> IBAN
    
    @Column(name = "debtor_account_other_id")
    private String debtorAccountOtherId;  // DbtrAcct -> Id -> Othr -> Id
    
    @Column(name = "debtor_agent_bic")
    private String debtorAgentBic;  // DbtrAgt -> FinInstnId -> BIC or BEI
    
    @Column(name = "debtor_agent_name")
    private String debtorAgentName;  // DbtrAgt -> FinInstnId -> Nm
    
    @Column(name = "debtor_address_line")
    private String debtorAddressLine;  // Dbtr -> PstlAdr -> AdrLine
    
    @Column(name = "debtor_country")
    private String debtorCountry;  // Dbtr -> PstlAdr -> Ctry

    // Creditor (Odbiorca)
    @Column(name = "creditor_name")
    private String creditorName;  // Cdtr -> Nm
    
    @Column(name = "creditor_legal_name")
    private String creditorLegalName;  // Cdtr -> Id -> OrgId -> LEI
    
    @Column(name = "creditor_account_iban")
    private String creditorAccountIban;  // CdtrAcct -> Id -> IBAN
    
    @Column(name = "creditor_account_other_id")
    private String creditorAccountOtherId;  // CdtrAcct -> Id -> Othr -> Id
    
    @Column(name = "creditor_agent_bic")
    private String creditorAgentBic;  // CdtrAgt -> FinInstnId -> BIC or BEI
    
    @Column(name = "creditor_agent_name")
    private String creditorAgentName;  // CdtrAgt -> FinInstnId -> Nm
    
    @Column(name = "creditor_address_line")
    private String creditorAddressLine;  // Cdtr -> PstlAdr -> AdrLine
    
    @Column(name = "creditor_country")
    private String creditorCountry;  // Cdtr -> PstlAdr -> Ctry

    // Transaction Amount Details
    @Column(name = "instructed_amount", precision = 19, scale = 4)
    private BigDecimal instructedAmount;  // Amt -> InstdAmt
    
    @Column(name = "instructed_amount_currency", length = 3)
    private String instructedAmountCurrency;  // Amt -> InstdAmt @Ccy
    
    @Column(name = "equivalent_amount", precision = 19, scale = 4)
    private BigDecimal equivalentAmount;  // Amt -> EqvtAmt
    
    @Column(name = "equivalent_amount_currency", length = 3)
    private String equivalentAmountCurrency;  // Amt -> EqvtAmt @Ccy

    // Settlement
    @Column(name = "settlement_date")
    private LocalDate settlementDate;  // IntrBkSttlmDt
    
    @Column(name = "settlement_instruction_id", length = 35)
    private String settlementInstructionId;  // SttlmInf -> SttlmInstrId
    
    @Column(name = "settlement_method", length = 35)
    private String settlementMethod;  // SttlmInf -> SttlmMtd

    // Payment Purpose (Remittance Information)
    @Column(name = "remittance_reference", length = 35)
    private String remittanceReference;  // RmtInf -> Ref
    
    @Column(name = "remittance_unstructured")
    private String remittanceUnstructured;  // RmtInf -> Ustrd
    
    @Column(name = "remittance_structured_issuer")
    private String remittanceStructuredIssuer;  // RmtInf -> Strd -> CdtrRefInf -> RefIssuer
    
    @Column(name = "remittance_structured_type")
    private String remittanceStructuredType;  // RmtInf -> Strd -> CdtrRefInf -> Tp -> CdOrPrtry -> Cd
    
    @Column(name = "remittance_structured_reference")
    private String remittanceStructuredReference;  // RmtInf -> Strd -> CdtrRefInf -> Ref

    // Ultimate Debtor/Creditor (Ultmt)
    @Column(name = "ultimate_debtor_name")
    private String ultimateDebtorName;  // UltmtDbtr -> Nm
    
    @Column(name = "ultimate_debtor_account_iban")
    private String ultimateDebtorAccountIban;  // UltmtDbtrAcct -> Id -> IBAN
    
    @Column(name = "ultimate_creditor_name")
    private String ultimateCreditorName;  // UltmtCdtr -> Nm
    
    @Column(name = "ultimate_creditor_account_iban")
    private String ultimateCreditorAccountIban;  // UltmtCdtrAcct -> Id -> IBAN

    // Purpose
    @Column(name = "purpose_code", length = 4)
    private String purposeCode;  // Purp -> Cd (ISO20022 Purpose Codes)
    
    @Column(name = "purpose_proprietary")
    private String purposeProprietary;  // Purp -> Prtry

    // Regulatory Information
    @Column(name = "regulatory_reporting_country")
    private String regulatoryReportingCountry;  // RgltryRptg -> Ctry
    
    @Column(name = "regulatory_reporting_code")
    private String regulatoryReportingCode;  // RgltryRptg -> Dt -> Cd
    
    @Column(name = "regulatory_reporting_amount", precision = 19, scale = 4)
    private BigDecimal regulatoryReportingAmount;  // RgltryRptg -> Amt
    
    @Column(name = "regulatory_reporting_currency", length = 3)
    private String regulatoryReportingCurrency;  // RgltryRptg -> Amt @Ccy

    // Related Parties
    @Column(name = "initiating_party_name")
    private String initiatingPartyName;  // InitgPty -> Nm
    
    @Column(name = "initiating_party_legal_id")
    private String initiatingPartyLegalId;  // InitgPty -> Id -> OrgId -> LEI
    
    @Column(name = "previous_message_id", length = 35)
    private String previousMessageId;  // PrvsMsgInf -> PrvsMsgId -> MsgId
    
    @Column(name = "previous_message_name_id", length = 35)
    private String previousMessageNameId;  // PrvsMsgInf -> PrvsMsgId -> MsgNmId

    // Original Transaction Reference (for pacs.008)
    @Column(name = "original_transaction_id", length = 35)
    private String originalTransactionId;  // OrgnlTxRef -> TxId
    
    @Column(name = "original_instruction_id", length = 35)
    private String originalInstructionId;  // OrgnlTxRef -> InstrId
    
    @Column(name = "original_end_to_end_id", length = 35)
    private String originalEndToEndId;  // OrgnlTxRef -> EndToEndId
    
    @Column(name = "original_amount", precision = 19, scale = 4)
    private BigDecimal originalAmount;  // OrgnlTxRef -> Amt -> InstdAmt
    
    @Column(name = "original_amount_currency", length = 3)
    private String originalAmountCurrency;  // OrgnlTxRef -> Amt -> InstdAmt @Ccy

    // Legacy fields (for backward compatibility)
    @Column(name = "pain_message_id")
    private String painMessageId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_account", nullable = false)
    private String senderAccount;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_account", nullable = false)
    private String receiverAccount;

    @Column(name = "payment_title")
    private String paymentTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status;

    @Column(name = "authorized_by")
    private String authorizedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Transaction() {
        this.status = TransactionStatus.RECEIVED;
        this.createdAt = Instant.now();
    }

    // Getters and Setters for ISO20022 fields
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public Instant getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(Instant creationDateTime) { this.creationDateTime = creationDateTime; }
    
    public Integer getNumberOfTransactions() { return numberOfTransactions; }
    public void setNumberOfTransactions(Integer numberOfTransactions) { this.numberOfTransactions = numberOfTransactions; }
    
    public BigDecimal getControlSum() { return controlSum; }
    public void setControlSum(BigDecimal controlSum) { this.controlSum = controlSum; }
    
    public String getPaymentInstructionId() { return paymentInstructionId; }
    public void setPaymentInstructionId(String paymentInstructionId) { this.paymentInstructionId = paymentInstructionId; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public Boolean getBatchBooking() { return batchBooking; }
    public void setBatchBooking(Boolean batchBooking) { this.batchBooking = batchBooking; }
    
    public LocalDate getRequestedExecutionDate() { return requestedExecutionDate; }
    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) { this.requestedExecutionDate = requestedExecutionDate; }
    
    public String getChargeBearer() { return chargeBearer; }
    public void setChargeBearer(String chargeBearer) { this.chargeBearer = chargeBearer; }
    
    // Debtor
    public String getDebtorName() { return debtorName; }
    public void setDebtorName(String debtorName) { this.debtorName = debtorName; }
    
    public String getDebtorLegalName() { return debtorLegalName; }
    public void setDebtorLegalName(String debtorLegalName) { this.debtorLegalName = debtorLegalName; }
    
    public String getDebtorAccountIban() { return debtorAccountIban; }
    public void setDebtorAccountIban(String debtorAccountIban) { this.debtorAccountIban = debtorAccountIban; }
    
    public String getDebtorAccountOtherId() { return debtorAccountOtherId; }
    public void setDebtorAccountOtherId(String debtorAccountOtherId) { this.debtorAccountOtherId = debtorAccountOtherId; }
    
    public String getDebtorAgentBic() { return debtorAgentBic; }
    public void setDebtorAgentBic(String debtorAgentBic) { this.debtorAgentBic = debtorAgentBic; }
    
    public String getDebtorAgentName() { return debtorAgentName; }
    public void setDebtorAgentName(String debtorAgentName) { this.debtorAgentName = debtorAgentName; }
    
    public String getDebtorAddressLine() { return debtorAddressLine; }
    public void setDebtorAddressLine(String debtorAddressLine) { this.debtorAddressLine = debtorAddressLine; }
    
    public String getDebtorCountry() { return debtorCountry; }
    public void setDebtorCountry(String debtorCountry) { this.debtorCountry = debtorCountry; }
    
    // Creditor
    public String getCreditorName() { return creditorName; }
    public void setCreditorName(String creditorName) { this.creditorName = creditorName; }
    
    public String getCreditorLegalName() { return creditorLegalName; }
    public void setCreditorLegalName(String creditorLegalName) { this.creditorLegalName = creditorLegalName; }
    
    public String getCreditorAccountIban() { return creditorAccountIban; }
    public void setCreditorAccountIban(String creditorAccountIban) { this.creditorAccountIban = creditorAccountIban; }
    
    public String getCreditorAccountOtherId() { return creditorAccountOtherId; }
    public void setCreditorAccountOtherId(String creditorAccountOtherId) { this.creditorAccountOtherId = creditorAccountOtherId; }
    
    public String getCreditorAgentBic() { return creditorAgentBic; }
    public void setCreditorAgentBic(String creditorAgentBic) { this.creditorAgentBic = creditorAgentBic; }
    
    public String getCreditorAgentName() { return creditorAgentName; }
    public void setCreditorAgentName(String creditorAgentName) { this.creditorAgentName = creditorAgentName; }
    
    public String getCreditorAddressLine() { return creditorAddressLine; }
    public void setCreditorAddressLine(String creditorAddressLine) { this.creditorAddressLine = creditorAddressLine; }
    
    public String getCreditorCountry() { return creditorCountry; }
    public void setCreditorCountry(String creditorCountry) { this.creditorCountry = creditorCountry; }
    
    // Amount
    public BigDecimal getInstructedAmount() { return instructedAmount; }
    public void setInstructedAmount(BigDecimal instructedAmount) { this.instructedAmount = instructedAmount; }
    
    public String getInstructedAmountCurrency() { return instructedAmountCurrency; }
    public void setInstructedAmountCurrency(String instructedAmountCurrency) { this.instructedAmountCurrency = instructedAmountCurrency; }
    
    public BigDecimal getEquivalentAmount() { return equivalentAmount; }
    public void setEquivalentAmount(BigDecimal equivalentAmount) { this.equivalentAmount = equivalentAmount; }
    
    public String getEquivalentAmountCurrency() { return equivalentAmountCurrency; }
    public void setEquivalentAmountCurrency(String equivalentAmountCurrency) { this.equivalentAmountCurrency = equivalentAmountCurrency; }
    
    // Settlement
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    
    public String getSettlementInstructionId() { return settlementInstructionId; }
    public void setSettlementInstructionId(String settlementInstructionId) { this.settlementInstructionId = settlementInstructionId; }
    
    public String getSettlementMethod() { return settlementMethod; }
    public void setSettlementMethod(String settlementMethod) { this.settlementMethod = settlementMethod; }
    
    // Remittance
    public String getRemittanceReference() { return remittanceReference; }
    public void setRemittanceReference(String remittanceReference) { this.remittanceReference = remittanceReference; }
    
    public String getRemittanceUnstructured() { return remittanceUnstructured; }
    public void setRemittanceUnstructured(String remittanceUnstructured) { this.remittanceUnstructured = remittanceUnstructured; }
    
    public String getRemittanceStructuredIssuer() { return remittanceStructuredIssuer; }
    public void setRemittanceStructuredIssuer(String remittanceStructuredIssuer) { this.remittanceStructuredIssuer = remittanceStructuredIssuer; }
    
    public String getRemittanceStructuredType() { return remittanceStructuredType; }
    public void setRemittanceStructuredType(String remittanceStructuredType) { this.remittanceStructuredType = remittanceStructuredType; }
    
    public String getRemittanceStructuredReference() { return remittanceStructuredReference; }
    public void setRemittanceStructuredReference(String remittanceStructuredReference) { this.remittanceStructuredReference = remittanceStructuredReference; }
    
    // Ultimate
    public String getUltimateDebtorName() { return ultimateDebtorName; }
    public void setUltimateDebtorName(String ultimateDebtorName) { this.ultimateDebtorName = ultimateDebtorName; }
    
    public String getUltimateDebtorAccountIban() { return ultimateDebtorAccountIban; }
    public void setUltimateDebtorAccountIban(String ultimateDebtorAccountIban) { this.ultimateDebtorAccountIban = ultimateDebtorAccountIban; }
    
    public String getUltimateCreditorName() { return ultimateCreditorName; }
    public void setUltimateCreditorName(String ultimateCreditorName) { this.ultimateCreditorName = ultimateCreditorName; }
    
    public String getUltimateCreditorAccountIban() { return ultimateCreditorAccountIban; }
    public void setUltimateCreditorAccountIban(String ultimateCreditorAccountIban) { this.ultimateCreditorAccountIban = ultimateCreditorAccountIban; }
    
    // Purpose
    public String getPurposeCode() { return purposeCode; }
    public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }
    
    public String getPurposeProprietary() { return purposeProprietary; }
    public void setPurposeProprietary(String purposeProprietary) { this.purposeProprietary = purposeProprietary; }
    
    // Regulatory
    public String getRegulatoryReportingCountry() { return regulatoryReportingCountry; }
    public void setRegulatoryReportingCountry(String regulatoryReportingCountry) { this.regulatoryReportingCountry = regulatoryReportingCountry; }
    
    public String getRegulatoryReportingCode() { return regulatoryReportingCode; }
    public void setRegulatoryReportingCode(String regulatoryReportingCode) { this.regulatoryReportingCode = regulatoryReportingCode; }
    
    public BigDecimal getRegulatoryReportingAmount() { return regulatoryReportingAmount; }
    public void setRegulatoryReportingAmount(BigDecimal regulatoryReportingAmount) { this.regulatoryReportingAmount = regulatoryReportingAmount; }
    
    public String getRegulatoryReportingCurrency() { return regulatoryReportingCurrency; }
    public void setRegulatoryReportingCurrency(String regulatoryReportingCurrency) { this.regulatoryReportingCurrency = regulatoryReportingCurrency; }
    
    // Related Parties
    public String getInitiatingPartyName() { return initiatingPartyName; }
    public void setInitiatingPartyName(String initiatingPartyName) { this.initiatingPartyName = initiatingPartyName; }
    
    public String getInitiatingPartyLegalId() { return initiatingPartyLegalId; }
    public void setInitiatingPartyLegalId(String initiatingPartyLegalId) { this.initiatingPartyLegalId = initiatingPartyLegalId; }
    
    public String getPreviousMessageId() { return previousMessageId; }
    public void setPreviousMessageId(String previousMessageId) { this.previousMessageId = previousMessageId; }
    
    public String getPreviousMessageNameId() { return previousMessageNameId; }
    public void setPreviousMessageNameId(String previousMessageNameId) { this.previousMessageNameId = previousMessageNameId; }
    
    // Original Transaction
    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }
    
    public String getOriginalInstructionId() { return originalInstructionId; }
    public void setOriginalInstructionId(String originalInstructionId) { this.originalInstructionId = originalInstructionId; }
    
    public String getOriginalEndToEndId() { return originalEndToEndId; }
    public void setOriginalEndToEndId(String originalEndToEndId) { this.originalEndToEndId = originalEndToEndId; }
    
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    
    public String getOriginalAmountCurrency() { return originalAmountCurrency; }
    public void setOriginalAmountCurrency(String originalAmountCurrency) { this.originalAmountCurrency = originalAmountCurrency; }
    
    // Legacy getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPainMessageId() { return painMessageId; }
    public void setPainMessageId(String painMessageId) { this.painMessageId = painMessageId; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDate getValueDate() { return valueDate; }
    public void setValueDate(LocalDate valueDate) { this.valueDate = valueDate; }
    
    public String getSenderName() { return debtorName != null ? debtorName : senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderAccount() { return debtorAccountIban != null ? debtorAccountIban : senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }
    
    public String getReceiverName() { return creditorName != null ? creditorName : receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public String getReceiverAccount() { return creditorAccountIban != null ? creditorAccountIban : receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }
    
    public String getPaymentTitle() { 
        if (remittanceUnstructured != null) return remittanceUnstructured;
        return paymentTitle; 
    }
    public void setPaymentTitle(String paymentTitle) { this.paymentTitle = paymentTitle; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public String getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(String authorizedBy) { this.authorizedBy = authorizedBy; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

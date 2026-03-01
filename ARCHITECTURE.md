# ARCHITECTURE - System Przetwarzania Płatności ISO20022

## 1. Przegląd Architektury

### 1.1 Model wysokopoziomowy

System oparty na **Value Stream Architecture (VSA)** zorientowanej na strumienie przetwarzania transakcji płatniczych.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   REST API   │  │  Web UI     │  │   GUI CLI   │  │  WebSocket  │    │
│  │  (Spring)    │  │  (Static)   │  │  (Future)   │  │  (Events)    │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           APPLICATION LAYER                                │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                    API GATEWAY / CONTROLLER                         │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐               │  │
│  │  │ Transaction  │ │  Authorizer  │ │   Clearing   │               │  │
│  │  │  Controller  │ │  Controller  │ │  Controller  │               │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘               │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                    │                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                    APPLICATION SERVICES                               │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐               │  │
│  │  │ Transaction   │ │  Authorization │ │  Clearing    │               │  │
│  │  │   Service     │ │    Service     │ │   Service    │               │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘               │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DOMAIN LAYER                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                        DOMAIN MODEL                                 │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐               │  │
│  │  │ Transaction   │ │  Authorization│ │    Events     │               │  │
│  │  │   Entity     │ │    Rules      │ │   (Modulith)  │               │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘               │  │
│  │                                                                   │  │
│  │  ┌──────────────────────────────────────────────────────────────┐  │  │
│  │  │              AUTHORIZER SELECTOR (VSA Pattern)              │  │  │
│  │  │  ┌────────────┐ ┌────────────┐ ┌────────────┐              │  │  │
│  │  │  │ Automatic   │ │   Manual    │ │ Supervisor │              │  │  │
│  │  │  │ Authorizer │ │ Authorizer  │ │ Authorizer │              │  │  │
│  │  │  └────────────┘ └────────────┘ └────────────┘              │  │  │
│  │  └──────────────────────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE LAYER                                │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │   Repository  │ │    Parser    │ │  XML/XSD     │ │   Message    │  │
│  │   (JPA)      │ │   (ISO20022) │ │  Validator   │ │   Queue      │  │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘  │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                      PERSISTENCE                                      │ │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐               │ │
│  │  │    H2 DB     │ │   Liquibase  │ │  Flyway (N)   │               │ │
│  │  └──────────────┘ └──────────────┘ └──────────────┘               │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Warstwy Architektury

### 2.1 Presentation Layer

#### REST API (Controllers)
- `TransactionController` - endpoints dla transakcji
- `AuthorizerController` - endpoints dla zarządzania autoryzacją
- `ClearingController` - endpoints dla clearingu
- `HealthController` - health checks

#### Web UI (Static)
- `index.html` - główna strona
- `app.js` - logika frontend
- `styles.css` - style

#### WebSocket
- Real-time powiadomienia o zmianach statusów

### 2.2 Application Layer

#### Application Services

**TransactionService**
- `receivePayment(File)` - odbiór pliku pain.001
- `validateTransaction(id)` - walidacja transakcji
- `updateStatus(id, status)` - aktualizacja statusu
- `getTransactions(filter)` - pobranie listy transakcji
- `suspendTransaction(id)` - wstrzymanie
- `approveTransaction(id)` - zatwierdzenie
- `rejectTransaction(id, reason)` - odrzucenie
- `editTransaction(id, changes)` - edycja

**AuthorizationService**
- `authorize(transaction)` - autoryzacja transakcji
- `selectAuthorizer(transaction)` - wybór authorizera
- `approveManually(id, approverId)` - ręczne zatwierdzenie

**ClearingService**
- `generatePacs008(transactions)` - generowanie pacs.008
- `sendToClearing(transactions)` - wysyłka do clearingu

### 2.3 Domain Layer

#### Domain Entities

**Transaction**
```java
- id: Long
- painMessageId: String
- transactionId: String (from pain)
- amount: BigDecimal
- currency: String
- valueDate: LocalDate
- senderName: String
- senderAccount: String (IBAN)
- receiverName: String
- receiverAccount: String (IBAN)
- paymentTitle: String
- status: TransactionStatus
- createdAt: Instant
- updatedAt: Instant
- authorizedBy: String
- approvedBy: String
- rejectionReason: String
```

**TransactionStatus** (enum)
- RECEIVED
- VALIDATED
- VALIDATION_FAILED
- AUTHORIZING
- AUTHORIZED
- AUTHORIZATION_FAILED
- PENDING_APPROVAL
- APPROVED
- REJECTED
- SUSPENDED
- SENT_TO_CLEARING
- COMPLETED
- FAILED

#### Authorizer System (VSA Pattern)

**Authorizer Interface**
```java
public interface Authorizer {
    AuthorizationResult authorize(Transaction tx);
    boolean supports(Transaction tx);
}
```

**AutomaticAuthorizer**
- Dla kwot < 10,000 PLN
- Natychmiastowa autoryzacja
- Weryfikacja podstawowa

**ManualAuthorizer**
- Dla kwot 10,000-100,000 PLN
- Wymaga akceptacji operatora
- Pełna weryfikacja

**SupervisorAuthorizer**
- Dla kwot > 100,000 PLN
- Wymaga akceptacji przełożonego
- Dodatkowa weryfikacja bezpieczeństwa

**AuthorizerSelector**
- Wybiera odpowiedniego authorizera na podstawie reguł:
  1. Sprawdź kwotę
  2. Sprawdź walutę (PLN vs inne)
  3. Sprawdź typ transakcji

### 2.4 Infrastructure Layer

#### Repository Layer
- `TransactionRepository` - JPA repository dla transakcji
- `AuditLogRepository` - JPA repository dla logów audytowych

#### ISO 20022 Parser
- Parsowanie pain.001 do domain objects
- Generowanie pacs.008 z domain objects
- Walidacja XSD

#### Message Queue (Future)
- Kafka lub RabbitMQ dla asynchronicznego przetwarzania

---

## 3. Bezpieczeństwo

### 3.1 Autentykacja i Autoryzacja

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY ARCHITECTURE                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │    Login     │───▶│  JWT Token   │───▶│  Spring     │  │
│  │   Endpoint   │    │  Generation   │    │  Security   │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                │            │
│  ┌──────────────────────────────────────────────▼────────┐  │
│  │                  SECURITY FILTER CHAIN                │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐    │  │
│  │  │  CORS   │ │  CSRF   │ │   JWT   │ │  Input  │    │  │
│  │  │ Filter  │ │ Filter  │ │ Filter  │ │Validation│    │  │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    ROLES & PERMISSIONS                 │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐    │  │
│  │  │  ADMIN  │ │OPERATOR │ │APPROVER │ │VIEWER   │    │  │
│  │  │  Full   │ │Submit   │ │Approve  │ │ReadOnly │    │  │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Roles & Permissions

| Rola | GET | POST | PUT | DELETE | SUSPEND | APPROVE |
|------|-----|------|-----|--------|---------|---------|
| ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| OPERATOR | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| APPROVER | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ |
| VIEWER | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 3.3 Walidacja Bezpieczeństwa

**Input Validation**
- Wszystkie pola walidowane pod kątem formatu
- Sanityzacja danych wejściowych
- Ochrona przed SQL Injection (parametryzowane queries)
- Ochrona przed XSS (escapowanie output)

**IBAN Encryption**
- Numery kont przechowywane zaszyfrowane
- AES-256-GCM dla danych wrażliwych

**Audit Logging**
- Wszystkie operacje CRUD logowane
- Logi zawierają: timestamp, user, action, entity, before/after

---

## 4. Struktura Projektu

```
payment-processor/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/payment/
│   │   │   ├── PaymentProcessorApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── ModulithConfig.java
│   │   │   │   └── LiquibaseConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── AuthorizerController.java
│   │   │   │   └── ClearingController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── AuthorizationService.java
│   │   │   │   └── ClearingService.java
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionStatus.java
│   │   │   │   ├── AuthorizationResult.java
│   │   │   │   └── Events.java
│   │   │   │
│   │   │   ├── authorizer/
│   │   │   │   ├── Authorizer.java (interface)
│   │   │   │   ├── AuthorizerSelector.java
│   │   │   │   ├── AutomaticAuthorizer.java
│   │   │   │   ├── ManualAuthorizer.java
│   │   │   │   └── SupervisorAuthorizer.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   │
│   │   │   ├── iso/
│   │   │   │   ├── Pain001Parser.java
│   │   │   │   ├── Pacs008Generator.java
│   │   │   │   └── XsdValidator.java
│   │   │   │
│   │   │   └── security/
│   │   │       ├── JwtTokenProvider.java
│   │   │       ├── JwtAuthenticationFilter.java
│   │   │       └── SecurityUtils.java
│   │   │   │
│   │   │   └── validation/
│   │   │       ├── IbanValidator.java
│   │   │       ├── AmountValidator.java
│   │   │       └── DateValidator.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/changelog/
│   │       │   ├── db.changelog-master.xml
│   │       │   └── 001-initial-schema.xml
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── app.js
│   │       │   └── styles.css
│   │       └── xsd/
│   │           ├── pain.001.001.12.xsd
│   │           └── pacs.008.001.10.xsd
│   │
│   └── test/
│       └── java/com/example/payment/
│           ├── service/
│           │   ├── TransactionServiceTest.java
│           │   └── AuthorizationServiceTest.java
│           ├── authorizer/
│           │   └── AuthorizerSelectorTest.java
│           └── iso/
│               └── Pain001ParserTest.java
│
├── REQUIREMENTS.md
├── ARCHITECTURE.md
└── TASKS.md
```

---

## 5. Technologie

| Komponent | Technologia | Wersja |
|-----------|------------|---------|
| Framework | Spring Boot | 4.0.0 |
| Java | JDK | 25 |
| Build | Maven | 3.9+ |
| Database | H2 | 2.2 |
| Migration | Liquibase | 4.25 |
| Modularity | Spring Modulith | 2.0.3 |
| Security | Spring Security + JWT | - |
| Validation | Jakarta Validation | 3.0 |
| Testing | JUnit 5 + Mockito | - |
| ISO Parser | JAXB / Jackson XML | - |

---

## 6. Wersje

| Wersja | Data | Opis |
|--------|------|------|
| 1.0 | 2026-03-01 | Initial architecture |

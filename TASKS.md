# TASKS - Plan Prac

## Przegląd

Projekt podzielony na zadania zgodnie z metodologią incrementalną. Każde zadanie powinno być ukończone, przetestowane i wypchnięte do repo przed przejściem do następnego.

---

## Sprint 1: Fundamenty

### Zadanie 1.1: Setup projektu
- [ ] Utworzenie projektu Spring Boot 4 z Java 25
- [ ] Dodanie zależności: JPA, H2, Liquibase, Modulith
- [ ] Konfiguracja Liquibase z tabelą event_publications
- [ ] Podstawowa konfiguracja application.properties
- [ ] **TEST**: Uruchomienie aplikacji i sprawdzenie /actuator/health
- [ ] **COMMIT**: "Setup: Spring Boot project with dependencies"

### Zadanie 1.2: Konfiguracja bezpieczeństwa
- [ ] Dodanie Spring Security + JWT
- [ ] Utworzenie klas konfiguracyjnych SecurityConfig
- [ ] Utworzenie JwtTokenProvider
- [ ] Utworzenie JwtAuthenticationFilter
- [ ] Podstawowe endpointy autentykacji (/api/auth/login, /api/auth/register)
- [ ] **TEST**: Rejestracja i logowanie użytkownika
- [ ] **COMMIT**: "Add: Security configuration with JWT authentication"

### Zadanie 1.3: Struktura bazy danych
- [ ] Utworzenie tabeli transactions
- [ ] Utworzenie tabeli audit_logs
- [ ] Utworzenie tabeli users (dla autentykacji)
- [ ] **TEST**: Uruchomienie aplikacji z Liquibase migrations
- [ ] **COMMIT**: "Add: Database schema with Liquibase"

---

## Sprint 2: Domain Model

### Zadanie 2.1: Encja Transaction
- [ ] Utworzenie enuma TransactionStatus
- [ ] Utworzenie encji Transaction (JPA)
- [ ] Utworzenie TransactionRepository
- [ ] **TEST**: Testy jednostkowe dla Transaction entity
- [ ] **COMMIT**: "Add: Transaction entity and repository"

### Zadanie 2.2: ISO 20022 Parser (pain.001)
- [ ] Dodanie biblioteki do parsowania XML
- [ ] Utworzenie Pain001Parser
- [ ] Walidacja XSD
- [ ] Mapowanie na domain objects
- [ ] **TEST**: Parsowanie przykładowego pliku pain.001
- [ ] **COMMIT**: "Add: ISO20022 pain.001 parser"

### Zadanie 2.3: Walidacja transakcji
- [ ] Utworzenie IbanValidator
- [ ] Utworzenie AmountValidator
- [ ] Utworzenie DateValidator
- [ ] Integracja z TransactionService
- [ ] **TEST**: Testy walidatorów
- [ ] **COMMIT**: "Add: Transaction validation logic"

---

## Sprint 3: Authorization System (VSA)

### Zadanie 3.1: Authorizer Interface
- [ ] Utworzenie interfejsu Authorizer
- [ ] Utworzenie AuthorizerSelector
- [ ] Utworzenie enuma AuthorizationResult
- [ ] **TEST**: Testy selektora authorizerów
- [ ] **COMMIT**: "Add: Authorizer interface and selector"

### Zadanie 3.2: Authorizer Implementations
- [ ] Utworzenie AutomaticAuthorizer
- [ ] Utworzenie ManualAuthorizer
- [ ] Utworzenie SupervisorAuthorizer
- [ ] Implementacja logiki wyboru authorizera
- [ ] **TEST**: Testy wszystkich authorizerów
- [ ] **COMMIT**: "Add: Authorizer implementations"

### Zadanie 3.3: Authorization Service
- [ ] Utworzenie AuthorizationService
- [ ] Integracja z authorizerami
- [ ] Obsługa statusów transakcji
- [ ] **TEST**: Testy integracyjne autoryzacji
- [ ] **COMMIT**: "Add: Authorization service"

---

## Sprint 4: Clearing

### Zadanie 4.1: Pacs008 Generator
- [ ] Utworzenie Pacs008Generator
- [ ] Generowanie XML zgodnego z XSD pacs.008
- [ ] Mapowanie z Transaction na Pacs008 format
- [ ] **TEST**: Generowanie przykładowego pacs.008
- [ ] **COMMIT**: "Add: ISO20022 pacs.008 generator"

### Zadanie 4.2: Clearing Service
- [ ] Utworzenie ClearingService
- [ ] Integracja z generatorem pacs.008
- [ ] Obsługa wysyłki do clearingu (mock/interface)
- [ ] **TEST**: Testy clearing service
- [ ] **COMMIT**: "Add: Clearing service"

---

## Sprint 5: REST API

### Zadanie 5.1: Transaction Controller
- [ ] Utworzenie TransactionController
- [ ] Endpoint POST /api/transactions (odbiór pain.001)
- [ ] Endpoint GET /api/transactions (lista)
- [ ] Endpoint GET /api/transactions/{id}
- [ ] Endpoint PATCH /api/transactions/{id}/status
- [ ] **TEST**: Testy kontrolera
- [ ] **COMMIT**: "Add: Transaction REST API"

### Zadanie 5.2: Authorizer Controller
- [ ] Utworzenie AuthorizerController
- [ ] Endpoint POST /api/authorizations/{id}/approve
- [ ] Endpoint POST /api/authorizations/{id}/reject
- [ ] **TEST**: Testy kontrolera
- [ ] **COMMIT**: "Add: Authorizer REST API"

---

## Sprint 6: GUI

### Zadanie 6.1: HTML/CSS Static Files
- [ ] Utworzenie index.html
- [ ] Utworzenie styles.css
- [ ] Utworzenie app.js
- [ ] Podstawowy layout z listą transakcji
- [ ] **TEST**: Sprawdzenie w przeglądarce
- [ ] **COMMIT**: "Add: Static HTML/CSS/JS frontend"

### Zadanie 6.2: Frontend Functionality
- [ ] Pobieranie listy transakcji z API
- [ ] Filtrowanie i sortowanie
- [ ] Funkcjonalność wstrzymania (SUSPEND)
- [ ] Funkcjonalność edycji (EDIT)
- [ ] Funkcjonalność zatwierdzenia (APPROVE)
- [ ] **TEST**: Testy E2E w przeglądarce
- [ ] **COMMIT**: "Add: Frontend functionality"

---

## Sprint 7: Testy i Dokumentacja

### Zadanie 7.1: Testy jednostkowe
- [ ] Pokrycie > 70% kodu testami
- [ ] Testy serwisów
- [ ] Testy walidatorów
- [ ] Testy authorizerów
- [ ] **COMMIT**: "Add: Unit tests"

### Zadanie 7.2: Testy integracyjne
- [ ] Testy REST API
- [ ] Testy bazy danych
- [ ] Testy bezpieczeństwa
- [ ] **COMMIT**: "Add: Integration tests"

### Zadanie 7.3: Dokumentacja
- [ ] Aktualizacja README.md
- [ ] Opis API (OpenAPI/Swagger - opcjonalnie)
- [ ] Przykładowe pliki pain.001
- [ ] **COMMIT**: "Add: Documentation"

---

## Sprint 8: Polishowanie

### Zadanie 8.1: Code Review i Refactoring
- [ ] Przegląd kodu
- [ ] Naprawa code smells
- [ ] Optymalizacja wydajności
- [ ] **COMMIT**: "Refactor: Code improvements"

### Zadanie 8.2: Finalne testy E2E
- [ ] Testy end-to-end
- [ ] Testy bezpieczeństwa
- [ ] Testy wydajności
- [ ] **COMMIT**: "Final: E2E tests and polish"

---

## Szacowanie Czasu

| Sprint | Zadania | Szacowany czas |
|--------|---------|---------------|
| Sprint 1 | 1.1 - 1.3 | 2-3 godz |
| Sprint 2 | 2.1 - 2.3 | 3-4 godz |
| Sprint 3 | 3.1 - 3.3 | 3-4 godz |
| Sprint 4 | 4.1 - 4.2 | 2-3 godz |
| Sprint 5 | 5.1 - 5.2 | 2-3 godz |
| Sprint 6 | 6.1 - 6.2 | 3-4 godz |
| Sprint 7 | 7.1 - 7.3 | 2-3 godz |
| Sprint 8 | 8.1 - 8.2 | 1-2 godz |

**Łączny szacowany czas: 18-26 godzin**

---

## Wersje

| Wersja | Data | Opis |
|--------|------|------|
| 1.0 | 2026-03-01 | Initial task plan |

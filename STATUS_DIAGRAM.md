# Nowe Wymaganie: Status Flow Diagram

## Opis
Above the transaction list, display a visual diagram showing the flow between statuses. Each status should show the count of transactions in that status. Clicking on a status should filter the list to show only transactions with that status.

## Szczegóły
1. **Status Flow Diagram** - wizualny diagram przepływu statusów nad listą transakcji
2. **Licznik** - każdy status pokazuje liczbę transakcji
3. **Filtrowanie** - kliknięcie w status filtruje listę

## Statusy transakcji
RECEIVED → VALIDATED → AUTHORIZING → AUTHORIZED → PENDING_APPROVAL → APPROVED → SENT_TO_CLEARING → COMPLETED

Z podSTATUSami:
- VALIDATION_FAILED (od RECEIVED)
- AUTHORIZATION_FAILED (od AUTHORIZING)
- REJECTED (od PENDING_APPROVAL)
- SUSPENDED (może być w wielu miejscach)
- FAILED (od SENT_TO_CLEARING)

## Implementacja
1. Zaktualizować TASKS.md
2. Zmodyfikować index.html - dodać kontener na diagram
3. Zmodyfikować styles.css - style dla diagramu
4. Zmodyfikować app.js - pobieranie liczników i filtrowanie
5. Zbudować i przetestować

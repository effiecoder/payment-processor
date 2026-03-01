# REQUIREMENTS - System Przetwarzania Płatności ISO20022

## 1. Wprowadzenie

### 1.1 Cel dokumentu
Dokument określa wymagania funkcjonalne i niefunkcjonalne dla systemu przetwarzania płatności zgodnego ze standardem ISO20022.

### 1.2 Zakres systemu
System służy do:
- Odbierania transakcji płatniczych w formacie ISO20022 pain.001
- Walidacji transakcji pod kątem poprawności danych
- Autoryzacji transakcji przez dedykowane authorizery
- Przekazywania autoryzowanych transakcji do systemu clearingowego w formacie pacs.008

---

## 2. Wymagania Funkcjonalne

### 2.1 Odbiór transakcji (pain.001)

**F1** - System musi umożliwiać przesłanie pliku XML w formacie pain.001.001.12 (CustomerCreditTransferInitiationV12)

**F2** - System musi parsować i walidować strukturę XML zgodnie ze schematem ISO20022

**F3** - Dla każdej transakcji (CreditTransferTransactionAndStatus) muszą być wyodrębnione:
- Identyfikator transakcji (TransactionId)
- Kwota (Amount - IsoAmount)
- Waluta (Amount/@Ccy)
- Data waluty (DateAndDateTime2Choice/Date)
- Nazwa i rachunek nadawcy (Dbtr/Nm, DbtrAcct/Id/IBAN)
- Nazwa i rachunek odbiorcy (Cdtr/Nm, CdtrAcct/Id/IBAN)
- Tytuł płatności (RmtInf/Ustrd)

**F4** - System musi obsługiwać wiele transakcji w jednym komunikacie pain.001 (pain.001 może zawierać wiele PaymentInformation)

**F5** - Każda transakcja musi mieć unikalny identyfikator systemowy po stronie odbiorcy

### 2.2 Walidacja transakcji

**F6** - Walidacja IBAN (format, suma kontrolna)
- Walidacja długości IBAN dla danego kraju
- Walidacja sumy kontrolnej MOD-97

**F7** - Walidacja kwoty
- Kwota nie może być ujemna ani zero
- Kwota nie może przekraczać limitu dziennego (konfigurowalne)
- Walidacja formatu (2 miejsca po przecinku)

**F8** - Walidacja daty
- Data waluty nie może być wcześniejsza niż data bieżąca
- Data waluty nie może być dalsza niż X dni (konfigurowalne)

**F9** - Walidacja tytułu płatności
- Maksymalna długość 140 znaków
- Zakaz znaków specjalnych (SQL injection, XSS)

### 2.3 Autoryzacja transakcji

**F10** - Każda transakcja musi przejść przez proces autoryzacji

**F11** - Authorizery dobierane na podstawie:
- Typu transakcji (przelew krajowy, zagraniczny, pilny)
- Kwoty transakcji (mała, średnia, duża)
- Waluty (PLN, EUR, USD, inne)
- Kanału (API, plik, manual)

**F12** - Typy authorizerów:
- AUTOMATIC - dla niskich kwot, niskiego ryzyka
- MANUAL - dla średnich kwot, wymaga akceptacji człowieka
- SUPERVISOR - dla wysokich kwot, wymaga akceptacji przełożonego

**F13** - Logika wyboru authorizera:
- Kwota < 10,000 PLN: AUTOMATIC
- Kwota 10,000-100,000 PLN: MANUAL
- Kwota > 100,000 PLN: SUPERVISOR
- Waluta != PLN: zawsze MANUAL (dodatkowa weryfikacja)
- Transakcja zagraniczna: zawsze SUPERVISOR

### 2.4 Przetwarzanie transakcji

**F14** - Statusy transakcji:
- RECEIVED - transakcja odebrana
- VALIDATED - walidacja przeszła pomyślnie
- VALIDATION_FAILED - walidacja nie powiodła się
- AUTHORIZING - w trakcie autoryzacji
- AUTHORIZED - autoryzacja przyznana
- AUTHORIZATION_FAILED - autoryzacja odrzucona
- PENDING_APPROVAL - oczekuje na ręczne zatwierdzenie
- APPROVED - zatwierdzona przez operatora
- REJECTED - odrzucona przez operatora
- SUSPENDED - wstrzymana przez operatora
- SENT_TO_CLEARING - wysłana do systemu clearingowego
- COMPLETED - transakcja zakończona pomyślnie
- FAILED - transakcja nie powiodła się

**F15** - Transakcje w statusie AUTHORIZED lub APPROVED są automatycznie wysyłane do clearingu

### 2.5 Zarządzanie transakcjami (GUI)

**F16** - Lista transakcji
- Wyświetlanie wszystkich transakcji z paginacją
- Filtrowanie po: statusie, dacie, kwocie, nadawcy, odbiorcy
- Sortowanie po dowolnej kolumnie
- Wyszukiwanie po ID transakcji

**F17** - Wstrzymywanie transakcji (SUSPENDED)
- Operator może wstrzymać dowolną transakcję w statusie PENDING_APPROVAL
- Wstrzymana transakcja nie jest wysyłana do clearingu

**F18** - Edycja transakcji
- Operator może edytować wybrane pola: kwota, data waluty, tytuł
- Po edycji transakcja wraca do statusu VALIDATED
- Historia zmian musi być logowana

**F19** - Zatwierdzanie transakcji
- Operator zatwierdza transakcje w statusie PENDING_APPROVAL
- Zatwierdzenie zmienia status na APPROVED
- Transakcja jest następnie wysyłana do clearingu

**F20** - Odrzucanie transakcji
- Operator może odrzucić transakcję z dowolnym statusem (poza COMPLETED/FAILED)
- Wymaga podania powodu odrzucenia

### 2.6 Generowanie pacs.008

**F21** - System musi generować komunikaty pacs.008.001.10 (FIToFICustomerCreditTransferV10)

**F22** - Dla każdej autoryzowanej transakcji generowany jest osobny wpis w pacs.008

**F23** - Komunikat musi zawierać:
- Identyfikator wiadomości (MessageId - UUID)
- Data i czas utworzenia (CreationDateTime)
- Identyfikator inicjatora (InitiatingParty)
- Dla każdej transakcji:
  - Identyfikator transakcji (TransactionId)
  - Kwota i waluta (Amount/InterbankSettlementAmount)
  - Data waluty (InterbankSettlementDate)
  - Dane nadawcy (DebtorAgent, Debtor)
  - Dane odbiorcy (CreditorAgent, Creditor)
  - Szczegóły płatności (PaymentTaskInformation/RemittanceInformation)

---

## 3. Wymagania Niefunkcjonalne

### 3.1 Wydajność

**NF1** - System musi obsłużyć minimum 1000 transakcji na sekundę

**NF2** - Czas przetwarzania pojedynczej transakcji (bez autoryzacji) < 100ms

**NF3** - Czas odpowiedzi API dla listy transakcji (1000 rekordów) < 2s

**NF4** - Autoryzacja automatyczna < 50ms

### 3.2 Bezpieczeństwo

**NF5** - Wszystkie endpoints REST wymagają autentykacji (JWT)

**NF6** - Hasła hashowane algorytmem bcrypt (cost factor 12)

**NF7** - Sesje JWT wygasają po 15 minutach, refresh token po 7 dniach

**NF8** - Wszystkie operacje audytowane (kto, co, kiedy)

**NF9** - Walidacja wejść (input validation) dla wszystkich pól

**NF10** - Ochrona przed SQL Injection, XSS, CSRF

**NF11** - Szyfrowanie danych wrażliwych w bazie (numery kont)

### 3.3 Niezawodność

**NF12** - Dostępność systemu: 99.9% ( SLA)

**NF13** - Automatyczne ponawianie operacji przy błędach sieciowych (3 próby)

**NF14** - Transakcje muszą być idempotentne (na podstawie PainId + TransactionId)

**NF15** - Recovery po awarii: max 5 minut na przywrócenie pełnej funkcjonalności

### 3.4 Skalowalność

**NF16** - Architektura pozwala na poziome skalowanie (stateless services)

**NF17** - Obsługa kolejek dla transakcji (asynchroniczne przetwarzanie)

### 3.5 Zgodność

**NF18** - Zgodność ze standardem ISO 20022 (pain.001, pacs.008)

**NF19** - Walidacja XML względem schematów XSD

---

## 4. Przypadki Użycia

### UC1: Przesłanie pliku pain.001
1. Użytkownik przesyła plik XML przez API lub GUI
2. System parsuje plik
3. Dla każdej transakcji:
   - Sprawdza poprawność XML
   - Tworzy wpis w bazie ze statusem RECEIVED
4. System zwraca potwierdzenie z liczbą przetworzonych transakcji

### UC2: Automatyczna autoryzacja
1. Transakcja przechodzi walidację (status VALIDATED)
2. System wybiera authorizera na podstawie reguł
3. Dla AUTOMATIC: natychmiast autoryzuje (status AUTHORIZED)
4. System generuje pacs.008 i wysyła do clearingu (status SENT_TO_CLEARING)

### UC3: Ręczna autoryzacja
1. Transakcja przechodzi walidację
2. System wybiera authorizera MANUAL/SUPERVISOR
3. Status zmienia się na PENDING_APPROVAL
4. Operator przegląda transakcję w GUI
5. Operator zatwierdza lub odrzuca
6. Status odpowiednio zmienia się na APPROVED lub REJECTED

### UC4: Wstrzymanie transakcji
1. Operator wybiera transakcję w GUI
2. Klika przycisk "Wstrzymaj"
3. System zmienia status na SUSPENDED
4. Transakcja nie jest wysyłana do clearingu

### UC5: Edycja transakcji
1. Operator wybiera transakcję w statusie PENDING_APPROVAL
2. Klika przycisk "Edytuj"
3. System wyświetla formularz z danymi
4. Operator modyfikuje pola
5. System zapisuje zmiany i ustawia status VALIDATED
6. Historia zmian jest logowana

---

## 5. Słownik Pojęć

| Pojęcie | Definicja |
|---------|-----------|
| ISO 20022 | Międzynarodowy standard formatów komunikatów finansowych |
| pain.001 | Format komunikatu przesyłania środków (credit transfer) |
| pacs.008 | Format komunikatu przesyłania środków do systemu clearingowego |
| Authorizer | Moduł/system odpowiedzialny za autoryzację transakcji |
| Clearing | Proces rozliczania transakcji między bankami |
| VSA | Value Stream Architecture - architektura oparta na strumieniach wartości |

---

## 6. Wersje

| Wersja | Data | Opis |
|--------|------|------|
| 1.0 | 2026-03-01 | Initial version |

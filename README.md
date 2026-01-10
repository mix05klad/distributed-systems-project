# PetCare – Online Pet Management & Appointment System

Το **PetCare** είναι μια διαδικτυακή εφαρμογή διαχείρισης κατοικιδίων και ραντεβού με κτηνίατρους.
Αναπτύχθηκε στο πλαίσιο του μαθήματος **Κατανεμημένα Συστήματα & Υπηρεσίες** και υλοποιεί λειτουργικότητα για δύο ρόλους (**Owners** και **Vets**), με στόχο την οργάνωση ραντεβού, ιστορικού επισκέψεων και ειδοποιήσεων.

---

## Ρόλοι & Λειτουργικότητες

### Ιδιοκτήτης (Owner)
- Εγγραφή / Σύνδεση
- Καταχώρηση και διαχείριση κατοικιδίων (είδος, ράτσα, ηλικία)
- Κλείσιμο ραντεβού με βάση τη διαθεσιμότητα κτηνίατρου
- Προβολή ιστορικού ολοκληρωμένων επισκέψεων/εμβολίων (Pet History)

### Κτηνίατρος (Vet)
- Διαχείριση διαθεσιμότητας (availability slots)
- Επιβεβαίωση / ακύρωση ραντεβού
- Ολοκλήρωση ραντεβού (mark as COMPLETED)
- Καταχώρηση σημειώσεων επίσκεψης (visit notes) για ολοκληρωμένα ραντεβού

---

## REST API & Swagger

Η εφαρμογή παρέχει REST API για βασικές λειτουργίες ώστε να μπορεί να καταναλωθεί από εξωτερικούς clients (Swagger/Postman/curl) και να υποστηρίζει μελλοντική διαλειτουργικότητα.

### Swagger UI (PetCare)
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### Authentication (JWT)
1. Κάνε login:
    - `POST /api/auth/login`
2. Πάρε το `token` από το response.
3. Στο Swagger πάτα **Authorize** και βάλε:
    - `Bearer <token>`

> Όλα τα προστατευμένα endpoints απαιτούν `Authorization: Bearer <token>`.

---

## Εξωτερικές Υπηρεσίες (External Services)

### HUANOC (black-box service – παρέχεται από τον διδάσκοντα)
Χρησιμοποιείται μέσω REST κλήσεων για:
- **Phone number validation / normalization (E.164)**
- **Αποστολή SMS** για ενημερώσεις ραντεβού

**Swagger UI (HUANOC):** http://localhost:8081/swagger-ui/index.html

Ενδεικτικά triggers ειδοποιήσεων:
- Νέο αίτημα ραντεβού προς κτηνίατρο
- Επιβεβαίωση ραντεβού προς ιδιοκτήτη
- Ακύρωση από κτηνίατρο προς ιδιοκτήτη
- Ολοκλήρωση ραντεβού προς ιδιοκτήτη
- Ενημέρωση visit notes προς ιδιοκτήτη

### Nager.Date (Public Holidays API)
Χρησιμοποιείται για **holiday warning** κατά το booking:
- Αν η ημερομηνία είναι επίσημη αργία, το ραντεβού καταχωρείται κανονικά αλλά εμφανίζεται προειδοποίηση στον χρήστη.

---

## Τεχνολογίες
- Java (Spring Boot)
- Spring MVC + Thymeleaf (UI)
- Spring Security + JWT (REST API Authentication)
- Spring Data JPA / Hibernate
- H2 Database
- Springdoc OpenAPI / Swagger UI
- External REST integrations: **HUANOC**, **Nager.Date**

---

## Εκτέλεση (Run)

### 1) Clone το repository της εξωτερικής υπηρεσίας
```bash
git clone https://github.com/gkoulis/DS-Lab-NOC
cd DS-Lab-NOC
```

### 2) Εκκίνηση NOC
```bash
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```
### 3) Open in browser: http://localhost:8081/swagger-ui/index.html
### 4) Clone το repository του Petcare
```bash
git clone https://github.com/mix05klad/distributed-systems-project
cd distributed-systems-project
```
### 5) Εκκίνηση του Petcare
```bash
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```
### 6) Open in browser: http://localhost:8080/index.html

## Demo Users (DataInitializer)
### Owners
- owner1/owner1pass
- owner2/owner2pass
### Vets
- vet1/vet1pass
- vet2/vet2pass
## Πρόσβαση στο σύστημα
- Swagger (Petcare): http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
## Ομάδα Ανάπτυξης
- Μιχαήλ Κλάδης (it2023029)
- Τουλούμη Δήμητρα (it2023074)
- Φιλιππίδης Χρήστος (it2023083)
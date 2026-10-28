# Gestionale Edile

Gestionale web per la gestione di imprese edili con Spring Boot 2.7.x + Oracle 12c + Maven.

## Struttura Progetto

```
gestionale-edile/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ src/
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ main/
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ java/com/edile/
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ entity/          # JPA Entities
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ repository/      # Data Access Layer
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ controller/      # REST Controllers
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ GestionaleEdileApplication.java
Ã¢â€â€š   Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ resources/
Ã¢â€â€š   Ã¢â€â€š       Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ application.properties
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ test/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ db/
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ init.sql                 # Script SQL per Oracle
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ pom.xml                      # Maven Configuration
```

## Prerequisiti

- **Java 8** (giÃƒÂ  installato)
- **Maven 3.6+** - [Scarica qui](https://maven.apache.org/download.cgi)
- **Oracle Database XE** - [Scarica qui](https://www.oracle.com/database/technologies/xe-downloads.html)
- **Oracle SQL Developer** (giÃƒÂ  hai)
- **VSCode** con Extension:
  - Extension Pack for Java
  - Maven for Java

## Setup Locale

### 1. Installare Maven

```bash
# Windows: Scarica e decomprimi da https://maven.apache.org/download.cgi
# Aggiungi al PATH di Windows
setx PATH "%PATH%;C:\apache-maven-3.9.x\bin"
mvn --version
```

### 2. Setup Database Oracle

Apri **SQL Developer** ed esegui lo script:
```bash
db/init.sql
```

### 3. Configurare Connessione Oracle

Modifica `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=system
spring.datasource.password=<tua_password>
```

### 4. Build e Run

```bash
# Build
mvn clean install

# Run (Tomcat embedded)
mvn spring-boot:run
```

### Avvio semplificato (Windows)

Per avviare sempre allo stesso modo con controllo automatico server su/giu:
```bat
avvia-gestionale.bat
```

Script utili per demo:
```bat
stop-gestionale.bat
riavvia-gestionale.bat
```

`avvia-gestionale.bat` ora rileva automaticamente JDK e Maven (anche se non sono nel PATH), avvia Spring Boot e aspetta la disponibilita della UI.

L'app sarÃƒÂ  disponibile su: **http://localhost:8080/api**

### UI Web (nuova)

- Login: **http://localhost:8080/api/index.html**
- Dashboard: **http://localhost:8080/api/dashboard.html**
- Moduli tabellari: `cantieri.html`, `anagrafica-personale.html`, `anagrafica-materiali.html`
- Menu personale: `personale.html` + `turni-personale.html`
- Import bolle: `carica-bolla-materiali.html`
- Utenze applicative: `utenti.html`
- Controllo cantiere: `controllo-cantiere.html` con sottomenu (`analisi-sprechi.html`, `kpi-cantiere.html`, `forecast-fabbisogni.html`, `report-essenziali.html`)

## Moduli Attuali

Ã¢Å“â€¦ **Cantieri** - CRUD + monitoraggio per cantiere  
Ã¢Å“â€¦ **Dipendenti** - CRUD completo  
Ã¢Å“â€¦ **Assegnazioni Personale** - assegnazione dipendenti ai cantieri  
Ã¢Å“â€¦ **Turni / Ore Lavorate** - gestione turni e totale ore per periodo  
Ã¢Å“â€¦ **Materiali** - catalogo, movimenti carico/scarico e giacenza  
Ã¢ÂÂ³ **Bolle Acquisto** - In development  

## Endpoints Esempio

```
GET  /api/cantieri                 # Lista tutti i cantieri
POST /api/cantieri                 # Crea nuovo cantiere
GET  /api/cantieri/{id}            # Dettagli cantiere
PUT  /api/cantieri/{id}            # Aggiorna cantiere
DELETE /api/cantieri/{id}          # Elimina cantiere
GET  /api/cantieri/{id}/attivita   # AttivitÃƒÂ  cantiere (WBS)
POST /api/cantieri/{id}/attivita   # Nuova attivitÃƒÂ  cantiere
PUT  /api/cantieri/{id}/attivita/{attivitaId}   # Aggiorna/completa attivitÃƒÂ 
POST /api/cantieri/{id}/attivita/{attivitaId}/materiali   # Materiali usati da attivitÃƒÂ 
GET  /api/cantieri/{id}/consuntivo # Consuntivo economico cantiere

GET  /api/dipendenti               # Lista dipendenti
POST /api/dipendenti               # Nuovo dipendente

GET  /api/assegnazioni/cantiere/{id}    # Personale assegnato al cantiere
POST /api/turni                          # Registra turno (workflow: ASSEGNATO/IN_CORSO/COMPLETATO)
GET  /api/turni/cantiere/{id}/ore?dal=2026-01-01&al=2026-01-31

POST /api/materiali/movimenti                    # Carico/scarico materiale
GET  /api/materiali/anagrafica?cantiereId={id}   # Vista anagrafica con quantitÃƒÂ 
POST /api/materiali/bolle/importa?cantiereId={id} # Upload bolla PDF/JPG con cantiere selezionato
GET  /api/materiali/bolle                        # Storico bolle importate

GET  /api/monitoraggio/cantieri/{id}    # KPI cantiere: ore, persone, materiali

POST /api/auth/login                          # Login con username/password
GET  /api/auth/validate                       # Validazione token sessione
GET  /api/utenti                              # Lista utenze applicative
POST /api/utenti                              # Crea utente (password cifrata BCrypt)
PUT  /api/utenti/{id}                         # Aggiorna ruolo/stato/dati utente
PUT  /api/utenti/{id}/password                # Aggiorna password utente

GET  /api/analisi/soglie                      # Lista soglie sprechi
POST /api/analisi/soglie                      # Crea soglia materiale/cantiere
PUT  /api/analisi/soglie/{id}                 # Aggiorna soglia
DELETE /api/analisi/soglie/{id}               # Elimina soglia
GET  /api/analisi/alert-sprechi               # Alert scorta bassa / consumo eccessivo
GET  /api/analisi/indicatori-materiali?cantiereId={id}                # Stato OK/ATTENZIONE/CRITICO e top rischio
GET  /api/analisi/kpi/materiali-cantiere?cantiereId={id}             # KPI previsto vs usato
GET  /api/analisi/forecast-fabbisogni?cantiereId={id}                # Forecast fabbisogni e riordino
GET  /api/analisi/report/essenziale?month=YYYY-MM                    # Report essenziale + base export CSV
GET  /api/analisi/report/comparativo-cantieri?month=YYYY-MM          # Report cantiere vs cantiere
GET  /api/analisi/report/mese-vs-mese?cantiereId={id}&months=6       # Report mese vs mese
```

Utente bootstrap di default (creato al primo avvio se assente):
- `username: admin`
- `password: Admin123!`

Formato righe import suggerito nella bolla (una riga per materiale):
`numeroBolla;materiale;quantita;peso;prezzoUnitario;cantiere`

## Prossimi Step

1. **Upload Bolle PDF** - Endpoint per caricamento e parsing
2. **Dashboard Frontend** - React/Angular con KPI operativi
3. **Autenticazione** - JWT security
4. **Report Costi** - costo ore + materiali per cantiere

## Note di Sviluppo

- Usa **Lombok** per ridurre boilerplate (@Data, @NoArgsConstructor)
- **JPA** con Hibernate per ORM
- **Oracle JDBC Driver** v11 per connessione
- **PDF parsing** con Apache PDFBox
- **Spring Data JPA** per repository pattern

---

**Sviluppato per**: Imprese Edili  
**Stack**: Spring Boot 2.7.x + Oracle 12c + Maven


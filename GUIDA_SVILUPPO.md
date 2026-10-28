# Guida sviluppo semplice - Gestionale Edile

Questa guida e pensata per fare modifiche veloci senza perdere tempo.

## Indice
1. [Dove sono i file](#1-dove-sono-i-file)
2. [Come funziona il flusso: HTML → JS → Controller → Database](#2-come-funziona-il-flusso-html--js--controller--database)
3. [Regola pratica: cosa toccare quando cambi qualcosa](#3-regola-pratica-cosa-toccare-quando-cambi-qualcosa)
4. [Esempio richiesto: funzione "Entra nel cantiere"](#4-mini-mappa-funzione---file)
5. [Checklist veloce prima di chiudere una modifica](#5-checklist-veloce-prima-di-chiudere-una-modifica)
6. [Come creare una nuova funzione in una pagina](#6-come-creare-una-nuova-funzione-in-una-pagina-esempio-elimina-cantiere)
7. [Come creare un nuovo menù](#7-come-creare-un-nuovo-menu)
8. [Debug: quando qualcosa non funziona](#8-debug-quando-qualcosa-non-funziona)

---

## 1) Dove sono i file

## Frontend (pagine)
Cartella: `src/main/resources/static`

- Login: `index.html`
- Dashboard/menu principale: `dashboard.html`
- Cantieri:
  - Lista: `cantieri.html`
  - Dettaglio operativo: `cantiere-dettaglio.html`
- Personale:
  - Menu: `personale.html`
  - Anagrafica: `anagrafica-personale.html`
  - Turni: `turni-personale.html`
- Materiali:
  - Menu: `materiali.html`
  - Anagrafica: `anagrafica-materiali.html`
  - Carica bolla: `carica-bolla-materiali.html`
- Utenze: `utenti.html`
- Controllo cantiere:
  - Menu: `controllo-cantiere.html`
  - Soglie/alert: `analisi-sprechi.html`
  - KPI: `kpi-cantiere.html`
  - Forecast: `forecast-fabbisogni.html`
  - Report essenziali: `report-essenziali.html`

Asset comuni frontend:
- Stili globali: `src/main/resources/static/assets/styles.css`
- Sessione/login client: `src/main/resources/static/assets/auth.js`

## Backend (API e logica)
Cartella: `src/main/java/com/edile`

- Controller API: `controller/*.java`
- Entita DB: `entity/*.java`
- Repository DB: `repository/*.java`
- Servizi: `service/*.java`
- Schema DB di riferimento: `db/init.sql`

Controller principali:
- Cantieri: `CantierController.java` -> `/api/cantieri`
- Materiali: `MaterialeController.java` -> `/api/materiali`
- Personale: `DipendenteController.java` -> `/api/dipendenti`
- Turni: `TurnoController.java` -> `/api/turni`
- Assegnazioni: `AssegnazioneCantiereController.java` -> `/api/assegnazioni`
- Monitoraggio: `MonitoraggioController.java` -> `/api/monitoraggio`
- Auth: `AuthController.java` -> `/api/auth`
- Utenti: `UtenteController.java` -> `/api/utenti`
- Analisi/KPI/forecast/report: `AnalisiSprechiController.java` -> `/api/analisi`

---

## 2) Come funziona il flusso: HTML → JS → Controller → Database

Questo è il pattern fondamentale di tutto il gestionale. Comprendere questo ti permette di fare qualsiasi modifica.

### Il Flusso Completo

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. FRONTEND (HTML + JavaScript)                                 │
│    - Pagina HTML statica (es: cantieri.html)                    │
│    - JavaScript carica i dati con fetch()                       │
│    - JavaScript mostra la grafica                               │
│    - Quando utente clicca bottone, JS chiama l'API              │
└─────────────────────────────────────────────────────────────────┘
                          ↓ fetch()
┌─────────────────────────────────────────────────────────────────┐
│ 2. BACKEND - CONTROLLER (Java Spring)                           │
│    - Riceve la richiesta HTTP (GET/POST/PUT/DELETE)             │
│    - Contiene la logica di business (validazioni, calcoli)      │
│    - Chiama il Repository per accedere al DB                    │
│    - Ritorna JSON al frontend                                   │
└─────────────────────────────────────────────────────────────────┘
                          ↓ Repository
┌─────────────────────────────────────────────────────────────────┐
│ 3. REPOSITORY (JPA)                                             │
│    - Interface che accede al Database                           │
│    - Genera automaticamente le query SQL                        │
│    - Ritorna i dati al Controller                               │
└─────────────────────────────────────────────────────────────────┘
                          ↓ Query SQL
┌─────────────────────────────────────────────────────────────────┐
│ 4. DATABASE (H2)                                                │
│    - Tabelle e dati veri                                        │
│    - Esegue SELECT/INSERT/UPDATE/DELETE                         │
│    - Ritorna i risultati al Repository                          │
└─────────────────────────────────────────────────────────────────┘
```

### Esempio Pratico: Eliminare un Cantiere

**Passo 1: L'utente clicca il bottone (HTML)**
```html
<button onclick="eliminaCantiere(1)">Elimina</button>
```

**Passo 2: JavaScript chiama l'API**
```javascript
function eliminaCantiere(id) {
    fetch("/api/cantieri/" + id, { method: "DELETE" })
        .then(() => {
            alert("Cantiere eliminato!");
            loadCantieri();  // ricarica la lista
        });
}
```

**Passo 3: Controller riceve la richiesta**
```java
@RestController
@RequestMapping("/api/cantieri")
public class CantierController {
    
    @DeleteMapping("/{id}")  // ← riceve DELETE /api/cantieri/1
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cantierRepository.deleteById(id);  // chiama il repository
        return ResponseEntity.noContent().build();  // ritorna 204 OK
    }
}
```

**Passo 4: Repository accede al Database**
```java
public interface CantierRepository extends JpaRepository<Cantiere, Long> {
    // deleteById() è fornito automaticamente da JpaRepository
    // Genera: DELETE FROM cantiere WHERE id = ?
}
```

**Passo 5: Database esegue la query**
```sql
DELETE FROM cantiere WHERE id = 1;
```

**Passo 6: JavaScript riceve la risposta e aggiorna la UI**
```javascript
// La funzione eliminaCantiere() riceve 204 OK
// Mostra "Cantiere eliminato!" e ricarica la lista
```

### Mapping tra Concetti

| Cosa vedi | Dove è | Cosa fa |
|---|---|---|
| Bottone/Link | HTML (pagina .html) | Trigger l'azione quando clicchi |
| `fetch()` | JavaScript in `<script>` | Chiama l'API backend |
| Endpoint API (es `/api/cantieri`) | Controller Java `@GetMapping`, `@PostMapping`, ecc | Riceve la richiesta e elabora |
| Metodo `deleteById()` | Repository `.java` | Accede al database |
| Query SQL (es `DELETE FROM cantiere`) | Database H2 | Esegue l'azione vera |

### Le 4 Operazioni Base (CRUD)

| Operazione | Metodo HTTP | Controller | Endpoint Esempio |
|---|---|---|---|
| **C**reate (creare) | POST | `@PostMapping` | `POST /api/cantieri` |
| **R**ead (leggere) | GET | `@GetMapping` | `GET /api/cantieri` |
| **U**pdate (modificare) | PUT | `@PutMapping` | `PUT /api/cantieri/1` |
| **D**elete (eliminare) | DELETE | `@DeleteMapping` | `DELETE /api/cantieri/1` |

### Come si traduce "nome metodo" in SQL

Spring genera la query SQL automaticamente dal nome del metodo Repository:

| Nome Metodo Java | SQL Generato |
|---|---|
| `findAll()` | `SELECT * FROM tabella;` |
| `findById(1)` | `SELECT * FROM tabella WHERE id = 1;` |
| `findByNomeIgnoreCase("cemento")` | `SELECT * FROM tabella WHERE UPPER(nome) = UPPER('cemento');` |
| `deleteById(1)` | `DELETE FROM tabella WHERE id = 1;` |

Quando il metodo non basta, usa `@Query` per scrivere JPQL manuale:
```java
@Query("SELECT m FROM Materiale m WHERE m.quantita < m.scorta")
List<Materiale> findSottoscorta();
```

---

## 3) Regola pratica: cosa toccare quando cambi qualcosa

- **Cambio solo testi/grafica pagina**
  - Tocca solo il file `.html` della pagina
  - Se stile globale: anche `assets/styles.css`

- **Cambio comportamento frontend (click, filtri, fetch, validazioni)**
  - Tocca il file `.html` della pagina (script JS interno)
  - Se e login/sessione: anche `assets/auth.js`

- **Cambio regole business o risposta API**
  - Tocca il `controller` corretto
  - Se serve, aggiorna anche `service`, `repository`, `entity`

- **Aggiungo un nuovo campo persistente**
  - `entity` + `repository` + `controller/service`
  - aggiorna anche UI
  - aggiorna `db/init.sql` (se usato per inizializzazione nuova istanza)

---

## 3) Esempio richiesto: funzione "Entra nel cantiere"

## Se vuoi cambiare bottone/link dalla lista cantieri
Tocca:
- `src/main/resources/static/cantieri.html`

Punto chiave:
- Link verso `cantiere-dettaglio.html?id=...`

## Se vuoi cambiare cosa succede dentro la pagina cantiere
Tocca:
- `src/main/resources/static/cantiere-dettaglio.html`

Punti chiave:
- caricamento dati cantiere
- creazione/completamento attivita
- aggiunta materiali ad attivita
- storico movimenti
- consuntivo

## Se vuoi cambiare la logica backend del cantiere
Tocca:
- `src/main/java/com/edile/controller/CantierController.java`

Endpoint coinvolti:
- `GET /api/cantieri/{cantiereId}/attivita`
- `POST /api/cantieri/{cantiereId}/attivita`
- `PUT /api/cantieri/{cantiereId}/attivita/{attivitaId}`
- `POST /api/cantieri/{cantiereId}/attivita/{attivitaId}/materiali`
- `GET /api/cantieri/{cantiereId}/attivita/{attivitaId}/movimenti`
- `GET /api/cantieri/{cantiereId}/consuntivo`

Se cambi i dati/strutture:
- anche `AttivitaCantiere`, `AttivitaMaterialeUtilizzo`, `MovimentoMateriale`, `MaterialeCatalogo`
- e i rispettivi repository

---

## 4) Mini-mappa "funzione -> file"

- Login/reveal password
  - Frontend: `index.html`, `assets/auth.js`
  - Backend: `AuthController.java`, `UtenteAppService.java`, `UtenteAppRepository.java`

- Gestione utenti
  - Frontend: `utenti.html`
  - Backend: `UtenteController.java`, `UtenteAppService.java`, `UtenteApp.java`

- Anagrafica cantieri
  - Frontend: `cantieri.html`
  - Backend: `CantierController.java`, `Cantiere.java`, `CantierRepository.java`

- Operazioni cantiere (attivita/materiali/consuntivo)
  - Frontend: `cantiere-dettaglio.html`
  - Backend: `CantierController.java` + entity/repository attivita e movimenti

- Materiali e bolle
  - Frontend: `anagrafica-materiali.html`, `carica-bolla-materiali.html`
  - Backend: `MaterialeController.java`, `MaterialeImportService.java`, `MovimentoMateriale.java`

- Personale e turni
  - Frontend: `anagrafica-personale.html`, `turni-personale.html`
  - Backend: `DipendenteController.java`, `TurnoController.java`, `AssegnazioneCantiereController.java`

- KPI/forecast/report/sprechi
  - Frontend: `analisi-sprechi.html`, `kpi-cantiere.html`, `forecast-fabbisogni.html`, `report-essenziali.html`
  - Backend: `AnalisiSprechiController.java`, `SogliaMateriale.java`, `MovimentoMaterialeRepository.java`

---

## 5) Checklist veloce prima di chiudere una modifica

1. Avvia: `avvia-gestionale.bat`
2. Prova la pagina modificata
3. Verifica endpoint chiamati da quella pagina (tab Network browser)
4. Ricontrolla flussi base:
   - login
   - lista cantieri
   - entra nel cantiere
   - materiali
   - report essenziali

Se questa checklist e OK, la modifica e pronta.

---

## 6) Come creare una nuova funzione nella pagina Cantieri

Segui sempre questo schema:

1. Decidi il tipo di funzione:
   - solo UI (es. bottone/filtro visuale)
   - UI + backend (es. operazione con salvataggio)

2. Frontend (pagina cantieri):
   - file: `src/main/resources/static/cantieri.html`
   - aggiungi bottone/input
   - aggiungi funzione JS (es. `async function nuovaAzioneCantiere(id) { ... }`)
   - se serve backend, usa `fetch("/api/cantieri/...")`

3. Backend (se serve logica nuova):
   - file: `src/main/java/com/edile/controller/CantierController.java`
   - aggiungi endpoint (`@GetMapping`, `@PostMapping`, `@PutMapping`, ...)
   - se cambiano dati/regole, aggiorna anche `service`, `repository`, `entity`

4. Se cambia struttura dati:
   - `entity/Cantiere.java` (o altre entity)
   - `repository/CantierRepository.java`
   - `db/init.sql` (se necessario)

5. Test rapido:
   - avvia app
   - prova la funzione da UI
   - controlla chiamata API in Network (F12)

Regola pratica:
Per una funzione operativa su cantiere, di solito tocchi:
- `cantieri.html`
- `CantierController.java`

---

## 6) Guida completa: Creare una funzione JS + Controller + Richiamare l'API

Questo è il pattern che userai **per ogni nuova funzione**. Ti faccio 4 esempi con i 4 metodi HTTP.

### Struttura generale

```
1. FILE HTML          → Aggiungi bottone/input
2. FUNZIONE JS        → Scrivi la logica che chiama l'API
3. CONTROLLER JAVA    → Scrivi l'endpoint che riceve la richiesta
4. REPOSITORY (se serve) → Scrivi query al database
```

---

### Esempio 1: GET - Leggere dati

**Caso d'uso:** Clicchi un bottone "Vedi dettagli" e carica i dati del cantiere

**1. HTML (bottone)**
```html
<button onclick="caricaDettagliCantiere(5)">Vedi dettagli</button>
<div id="dettagli"></div>
```

**2. JavaScript (funzione)**
```javascript
async function caricaDettagliCantiere(id) {
    const response = await fetch("/api/cantieri/" + id);  // GET
    const data = await response.json();
    
    if (data) {
        document.getElementById("dettagli").innerHTML = `
            <p>Nome: ${data.nome}</p>
            <p>Indirizzo: ${data.indirizzo}</p>
            <p>Budget: €${data.budget}</p>
        `;
    }
}
```

**3. Controller Java (endpoint)**
```java
@RestController
@RequestMapping("/api/cantieri")
public class CantierController {
    
    @GetMapping("/{id}")  // ← riceve GET /api/cantieri/5
    public ResponseEntity<Cantiere> getById(@PathVariable Long id) {
        Optional<Cantiere> cantiere = cantierRepository.findById(id);
        return cantiere.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
}
```

**4. Flusso HTTP**
```
Browser:  GET /api/cantieri/5
          ↓
Server:   Riceve richiesta
          → Cerca ID 5 nel DB
          → Ritorna JSON con i dati
          ↓
Browser:  Riceve i dati e li mostra in dettagli
```

---

### Esempio 2: POST - Creare nuovo record

**Caso d'uso:** Clicchi "Crea cantiere", riempi form, clicchi "Salva"

**1. HTML (form)**
```html
<input type="text" id="nome" placeholder="Nome cantiere">
<input type="text" id="indirizzo" placeholder="Indirizzo">
<input type="number" id="budget" placeholder="Budget">
<button onclick="creaCantiere()">Salva cantiere</button>
```

**2. JavaScript (funzione)**
```javascript
async function creaCantiere() {
    const nuovoCantiere = {
        nome: document.getElementById("nome").value,
        indirizzo: document.getElementById("indirizzo").value,
        budget: parseFloat(document.getElementById("budget").value)
    };
    
    const response = await fetch("/api/cantieri", {  // POST
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(nuovoCantiere)  // ← mandi i dati al server
    });
    
    if (response.ok) {
        alert("Cantiere creato!");
        location.reload();  // ricarica la pagina
    }
}
```

**3. Controller Java (endpoint)**
```java
@RestController
@RequestMapping("/api/cantieri")
public class CantierController {
    
    @PostMapping  // ← riceve POST /api/cantieri
    public ResponseEntity<Cantiere> create(@RequestBody Cantiere cantiere) {
        // @RequestBody = prende il JSON dal body e lo converte in oggetto
        Cantiere salvato = cantierRepository.save(cantiere);
        return ResponseEntity.ok(salvato);
    }
}
```

**4. Flusso HTTP**
```
Browser:  POST /api/cantieri
          Body: { "nome": "Cantiere nuovo", "indirizzo": "Via Roma", "budget": 50000 }
          ↓
Server:   Riceve i dati JSON
          → Valida i dati
          → Li salva nel DB
          → Ritorna JSON con il cantiere creato (ID incluso)
          ↓
Browser:  Riceve 200 OK e ricarica la pagina
```

---

### Esempio 3: PUT - Modificare un record

**Caso d'uso:** Clicchi "Modifica", cambi il nome, clicchi "Salva"

**1. HTML (form)**
```html
<input type="text" id="nuovoNome" placeholder="Nuovo nome">
<button onclick="modificaCantiere(5)">Salva modifiche</button>
```

**2. JavaScript (funzione)**
```javascript
async function modificaCantiere(id) {
    const cantiereMod = {
        nome: document.getElementById("nuovoNome").value
    };
    
    const response = await fetch("/api/cantieri/" + id, {  // PUT
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cantiereMod)
    });
    
    if (response.ok) {
        alert("Cantiere modificato!");
        location.reload();
    }
}
```

**3. Controller Java (endpoint)**
```java
@RestController
@RequestMapping("/api/cantieri")
public class CantierController {
    
    @PutMapping("/{id}")  // ← riceve PUT /api/cantieri/5
    public ResponseEntity<Cantiere> update(@PathVariable Long id, 
                                          @RequestBody Cantiere cantiere) {
        cantiere.setId(id);  // assicura che modifica il cantiere giusto
        Cantiere aggiornato = cantierRepository.save(cantiere);
        return ResponseEntity.ok(aggiornato);
    }
}
```

**4. Flusso HTTP**
```
Browser:  PUT /api/cantieri/5
          Body: { "nome": "Cantiere rinominato" }
          ↓
Server:   Riceve i dati per ID 5
          → Cerca il cantiere con ID 5
          → Aggiorna i campi
          → Salva nel DB
          → Ritorna i dati aggiornati
          ↓
Browser:  Riceve 200 OK e ricarica
```

---

### Esempio 4: DELETE - Eliminare un record

**Caso d'uso:** Clicchi "Elimina" su una riga

**1. HTML (bottone in riga)**
```html
<button onclick="eliminaCantiere(5)" style="background-color: #dc3545;">Elimina</button>
```

**2. JavaScript (funzione)**
```javascript
async function eliminaCantiere(id) {
    if (!confirm("Sei sicuro di eliminare questo cantiere?")) {
        return;  // utente ha detto no
    }
    
    const response = await fetch("/api/cantieri/" + id, {  // DELETE
        method: "DELETE"  // No body, solo il metodo
    });
    
    if (response.ok) {
        alert("Cantiere eliminato!");
        location.reload();
    }
}
```

**3. Controller Java (endpoint)**
```java
@RestController
@RequestMapping("/api/cantieri")
public class CantierController {
    
    @DeleteMapping("/{id}")  // ← riceve DELETE /api/cantieri/5
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cantierRepository.deleteById(id);  // elimina dal DB
        return ResponseEntity.noContent().build();  // 204 OK
    }
}
```

**4. Flusso HTTP**
```
Browser:  DELETE /api/cantieri/5
          ↓
Server:   Riceve richiesta
          → Trova il cantiere con ID 5
          → Lo cancella dal DB
          → Ritorna 204 No Content
          ↓
Browser:  Riceve 204 OK e ricarica la pagina
```

---

### Riepilogo: I 4 Metodi HTTP

| Metodo | Per fare | Esempio Endpoint | Ha Body? | Ritorna |
|---|---|---|---|---|
| **GET** | Leggere dati | `GET /api/cantieri/5` | No | JSON con i dati |
| **POST** | Creare nuovo | `POST /api/cantieri` | Sì | JSON nuovo record |
| **PUT** | Modificare | `PUT /api/cantieri/5` | Sì | JSON record aggiornato |
| **DELETE** | Eliminare | `DELETE /api/cantieri/5` | No | Vuoto (204 OK) |

### UI pulita per le selezioni multiple

Se devi cancellare più record, usa questo layout:

- **checkbox in una colonna separata**
- **checkbox “seleziona tutto” nell’intestazione**
- **pulsante azione sopra la tabella**
- evita testo duplicato come “Seleziona” dentro ogni riga

Esempio:
```html
<th><input type="checkbox" id="selectAllCantieri"></th>
...
<td><input type="checkbox" class="cantiere-checkbox" value="${item.id}"></td>
```

---

### Checklist veloce per nuova funzione

```
☐ 1. Ho scritto il bottone/input nel file .html
☐ 2. Ho scritto la funzione JS che chiama fetch()
☐ 3. Ho scelto il metodo HTTP giusto (GET/POST/PUT/DELETE)
☐ 4. Ho scritto l'endpoint nel Controller con @GetMapping/@PostMapping/@PutMapping/@DeleteMapping
☐ 5. Ho testato in browser (F12 → Network) che la chiamata arriva
☐ 6. Ho controllato che i dati ritornano correttamente
☐ 7. Ho ricaricato la pagina e visto se la modifica persiste
```

---

## 7) Come creare un nuovo menù

Quando vuoi aggiungere una **nuova sezione principale** (come "Magazzino", "Fornitori", "Fatturazione", ecc.), devi toccare **6 file**:

### Passo 1: Creare il file HTML della pagina menù

**File:** `src/main/resources/static/nuomenu.html` (sostituisci "nuomenu" con il tuo nome)

```html
<!doctype html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nuovo Menù - Gestionale Edile</title>
    <link rel="stylesheet" href="assets/styles.css">
</head>
<body>
<main class="page">
    <section class="topbar">
        <div>
            <h1 class="title">Nuovo Menù</h1>
            <div class="muted">Descrizione di quello che puoi fare qui.</div>
        </div>
        <button class="btn" onclick="signOut()">Logout</button>
    </section>

    <nav class="nav">
        <a href="dashboard.html">Dashboard</a>
        <a href="cantieri.html">Cantieri</a>
        <a href="personale.html">Personale</a>
        <a href="materiali.html">Materiali</a>
        <a href="utenti.html">Utenze</a>
        <a href="controllo-cantiere.html">Controllo Cantiere</a>
        <a class="active" href="nuomenu.html">Nuovo Menù</a>
    </nav>

    <section class="card" style="padding: 16px;">
        <div class="toolbar">
            <strong>Totale elementi: <span id="totalCount">0</span></strong>
            <button class="btn" onclick="caricaDati()">Aggiorna</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nome</th>
                    <th>Stato</th>
                    <th>Operazioni</th>
                </tr>
                </thead>
                <tbody id="rows"></tbody>
            </table>
            <div id="empty" class="empty" style="display:none;">Nessun dato disponibile.</div>
        </div>
    </section>
</main>

<script src="assets/auth.js"></script>
<script>
    requireAuth();

    async function caricaDati() {
        const rows = document.getElementById("rows");
        const empty = document.getElementById("empty");
        rows.innerHTML = "";

        const response = await fetch("/api/nuomenu");
        const data = await response.json();
        document.getElementById("totalCount").textContent = data.length;

        if (!data.length) {
            empty.style.display = "block";
            return;
        }
        empty.style.display = "none";

        rows.innerHTML = data.map(item => `
            <tr>
                <td>${item.id ?? ""}</td>
                <td>${item.nome ?? ""}</td>
                <td>${item.stato ?? ""}</td>
                <td><button class="btn" onclick="dettagli(${item.id})">Dettagli</button></td>
            </tr>
        `).join("");
    }

    function dettagli(id) {
        alert("Dettagli: " + id);
    }

    caricaDati();
</script>
</body>
</html>
```

---

### Passo 2: Aggiungere il link a TUTTE le altre pagine HTML

Apri **OGNI pagina HTML** e aggiungi il link nel menu `<nav>`:

```html
<a href="nuomenu.html">Nuovo Menù</a>  <!-- ← AGGIUNGI QUESTA RIGA -->
```

⚠️ **IMPORTANTE:** Se dimentichi anche una pagina, il link non comparirà!

---

### Passo 3: Creare il Controller Java

**File:** `src/main/java/com/edile/controller/NuomenùController.java`

```java
package com.edile.controller;

import com.edile.entity.NuovaEntita;
import com.edile.repository.NuovaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nuomenu")
@CrossOrigin(origins = "*")
public class NuomenùController {

    @Autowired
    private NuovaRepository repository;

    @GetMapping
    public ResponseEntity<List<NuovaEntita>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NuovaEntita> getById(@PathVariable Long id) {
        return ResponseEntity.ok(repository.findById(id).orElse(null));
    }

    @PostMapping
    public ResponseEntity<NuovaEntita> create(@RequestBody NuovaEntita entita) {
        return ResponseEntity.ok(repository.save(entita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NuovaEntita> update(@PathVariable Long id, @RequestBody NuovaEntita entita) {
        entita.setId(id);
        return ResponseEntity.ok(repository.save(entita));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### Passo 4: Creare l'Entity (classe del modello dati)

**File:** `src/main/java/com/edile/entity/NuovaEntita.java`

```java
package com.edile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "nuova_tabella")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuovaEntita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    private String stato;
}
```

---

### Passo 5: Creare il Repository

**File:** `src/main/java/com/edile/repository/NuovaRepository.java`

```java
package com.edile.repository;

import com.edile.entity.NuovaEntita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NuovaRepository extends JpaRepository<NuovaEntita, Long> {
    List<NuovaEntita> findByNomeContainingIgnoreCase(String nome);
    List<NuovaEntita> findByStato(String stato);
}
```

---

### Passo 6: Avviare e testare

1. Avvia l'app: `avvia-gestionale.bat`
2. Apri `http://localhost:8080/api/nuomenu.html`
3. Nel browser, vai su **F12 → Network tab**
4. Clicca "Aggiorna" e vedi se la chiamata a `/api/nuomenu` funziona

---

### ✅ Riepilogo: 6 file da creare/modificare

| # | File | Cosa contiene |
|---|---|---|
| 1 | `src/main/resources/static/nuomenu.html` | La pagina UI del nuovo menù |
| 2 | Tutte le altre pagine `.html` | Aggiungi il link nel `<nav>` |
| 3 | `src/main/java/com/edile/controller/NuomenùController.java` | Gli endpoint API |
| 4 | `src/main/java/com/edile/entity/NuovaEntita.java` | La classe del modello dati |
| 5 | `src/main/java/com/edile/repository/NuovaRepository.java` | Interface per accedere al DB |

---

### 🔧 Checklist per nuovo menù

```
☐ 1. Ho creato nuomenu.html in src/main/resources/static/
☐ 2. Ho aggiunto il link in TUTTE le pagine HTML
☐ 3. Ho creato NuomenùController.java 
☐ 4. Ho creato NuovaEntita.java
☐ 5. Ho creato NuovaRepository.java
☐ 6. Ho fatto Build: avvia-gestionale.bat
☐ 7. Ho testato GET /api/nuomenu nel browser (F12 → Network)
☐ 8. Il menu appare in tutte le pagine
☐ 9. I dati si caricano correttamente
```

---

## 8) Debug: quando qualcosa non funziona

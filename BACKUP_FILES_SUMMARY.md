# File Backup per Deployment Locale - Riepilogo

## 📦 File Creati

### 1. **LOCAL_SETUP.md**
Guida di setup per avviare il progetto in locale
- Requisiti
- Comandi build e avvio
- Accesso a H2 console
- Struttura cartelle
- Backup e restore da riga di comando

### 2. **DEPLOYMENT_LOCAL.md**
Guida completa per deployment locale con scelte architetturali
- ✅ Configurazione attuale
- 3 Opzioni di deploy (PC locale, Network, USB portatile)
- Checklist pre-deploy
- Sicurezza e backup settimanali
- Disaster recovery
- Monitoraggio
- Troubleshooting

### 3. **scripts/README.md**
Guida rapida agli script di backup/restore
- Quick start Windows e Linux/Mac
- Backup automatico (Task Scheduler, Cron)
- Struttura cartelle
- Troubleshooting

### 4. **scripts/start-local.bat** (Windows)
Script di avvio completo
- Build con `mvn clean package`
- Avvio JAR
- Interfaccia user-friendly con menu

### 5. **scripts/start-local.sh** (Linux/Mac)
Script di avvio per sistemi Unix
- Build con `mvn clean package`
- Avvio JAR
- Output colorato

### 6. **scripts/backup-local.bat** (Windows)
Backup del database H2 + configurazione
- Crea ZIP con timestamp
- Comprime db/gestionale-edile.h2.db
- Incluso frontend e pom.xml
- Archivio in backups/

### 7. **scripts/backup-local.sh** (Linux/Mac)
Backup per sistemi Unix
- Crea TAR.GZ con timestamp
- Comprime tutto il necessario
- Archivio in backups/

### 8. **scripts/restore-local.bat** (Windows)
Ripristino da backup ZIP
- Chiede conferma prima di sovrascrivere
- Riporta dati a stato precedente
- Instruzioni per riavvio

### 9. **scripts/restore-local.sh** (Linux/Mac)
Ripristino per sistemi Unix
- Chiede conferma interattiva
- Estrae archivio TAR.GZ
- Riporta a stato precedente

## 🔄 Flusso di Lavoro

```
┌─────────────────────────────────┐
│ Avvia applicazione              │
│ scripts\start-local.bat         │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ Lavora quotidianamente          │
│ http://localhost:8080/api/      │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ Fai backup (settimanale)        │
│ scripts\backup-local.bat        │
│ ✅ archivio creato              │
└──────────────┬──────────────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
┌─────────────────┐  ┌──────────────────┐
│ Tutto OK?       │  │ Errore/Perdita?  │
│ Continua lavoro │  │ Ripristina        │
│                 │  │ restore-local.bat │
└─────────────────┘  └──────────────────┘
```

## 📊 Struttura File di Backup

Un backup ZIP/TAR contiene:

```
gestionale-edile-20241028-1425.zip
├── db/
│   └── gestionale-edile.h2.db     # ← Il database con tutti i dati
├── src/main/resources/static/      # ← Tutte le pagine HTML/CSS/JS
│   ├── index.html
│   ├── anagrafica-personale.html
│   ├── nuovo-dipendente.html
│   ├── carica-bolla-materiali.html
│   └── assets/
│       └── styles.css
└── pom.xml                         # ← Configurazione build
```

## 🎯 Quando Usare Ogni Script

| Caso d'uso | Script |
|-----------|--------|
| Prima volta? Avvia app | `start-local.bat/sh` |
| Lavoro finito, salva dati | `backup-local.bat/sh` |
| Errore, recupera dati vecchi | `restore-local.bat/sh` |
| PC si rompe, nuovo PC | Copy USB → `restore-local.bat` → OK |

## ⚙️ Configurazione Tecnica

### application.properties (già configurato)
```properties
# Database H2 locale
spring.datasource.url=jdbc:h2:file:./db/gestionale-edile;MODE=Oracle
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa

# Port
server.port=8080

# Context
server.servlet.context-path=/api
```

### pom.xml (pronto per deploy)
```xml
<packaging>jar</packaging>  <!-- ✅ JAR con Tomcat embedded -->

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>  <!-- ✅ H2 database gratuito -->
</dependency>
```

## 🔐 Sicurezza File

Dopo aver fatto il backup, il file ZIP/TAR contiene:
- ✅ Dati cifrati? NO (MA: sono file locali, non online)
- ✅ Credenziali? NO (credenziali sono in memoria app)
- ✅ File sensibili? NO (solo database e frontend)

**Consiglio**: Tieni i backup in cartella privata, non online.

## 📈 Scalabilità

### Attuale (H2 locale)
- Utenti concurrent: 1-5
- Database size: fino a 500MB
- Performance: eccellente per singolo utente

### Se cresce il business
Puoi poi migrare a:
- PostgreSQL (Render.com)
- MySQL (Hosting provider)
- Oracle (Enterprise)

Ma per ora: **H2 locale è perfetto** 🎯

## ✅ Verifica Setup

```bash
# 1. Apri cmd/terminal
# 2. Vai in directory gestionale-edile
cd C:\Users\ludemart\OneDrive - Capgemini\Desktop\CE.MA\gestionale-edile

# 3. Leggi file
type LOCAL_SETUP.md
type DEPLOYMENT_LOCAL.md
type scripts\README.md

# 4. Avvia
scripts\start-local.bat

# 5. Accedi browser
http://localhost:8080/api/

# 6. Login
admin / admin

# 7. Fai backup
scripts\backup-local.bat

# 8. Verifica backup creato
dir backups
```

## 🎁 Bonus: Automazione Backup (opzionale)

### Windows Task Scheduler
Ogni domenica alle 22:00:
```
Programma: C:\path\to\scripts\backup-local.bat
```

### Linux/Mac Cron
```bash
# Ogni domenica alle 22:00
0 22 * * 0 cd ~/gestionale-edile && ./scripts/backup-local.sh
```

## 📝 Checklist Finale

- [x] Creati script backup/restore (BAT e SH)
- [x] Creati script di avvio (BAT e SH)
- [x] Creata documentazione LOCAL_SETUP.md
- [x] Creata documentazione DEPLOYMENT_LOCAL.md
- [x] Creata documentazione scripts/README.md
- [x] Verificato pom.xml (JAR con H2)
- [x] Verificato application.properties (H2 locale)
- [x] Struttura cartella /scripts/ pronta
- [x] Backup pronti a essere usati
- [ ] Primo backup eseguito (TU: esegui `scripts\backup-local.bat`)

---

**Pronto per il deployment locale!** 🚀
Esegui `scripts\start-local.bat` per iniziare.

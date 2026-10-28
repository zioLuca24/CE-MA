# 📑 Indice Documentazione Deployment

Benvenuto! Qui troverai una guida ai file creati per il deployment locale.

---

## 🎯 Leggi Questo PRIMA di Tutto

### **→ QUICK_START.md** ⭐⭐⭐
*Vai qui se vuoi avviare subito*

Contiene:
- ✅ Come avviare l'app in 30 secondi
- ✅ Primo accesso
- ✅ Primo backup
- ✅ Troubleshooting rapido

**Tempo di lettura**: 5 minuti

---

## 📖 Guide Dettagliate

### **→ LOCAL_SETUP.md**
*Guida di setup tecnico per environment locale*

Contiene:
- Requisiti sistema
- Comandi build e avvio
- Accesso H2 console
- Struttura cartelle
- Backup e restore da terminale

**Tempo di lettura**: 10 minuti
**Per chi**: Sviluppatori, tecnici

---

### **→ DEPLOYMENT_LOCAL.md** ⭐
*Decisioni architetturali e strategie di deployment*

Contiene:
- ✅ Perché deployment locale è la scelta migliore
- ✅ 3 Opzioni di deploy (PC, Network, USB)
- ✅ Checklist pre-deploy
- ✅ Sicurezza e backup settimanali
- ✅ Disaster recovery
- ✅ Quando migrare a cloud (futuro)

**Tempo di lettura**: 15 minuti
**Per chi**: Decision makers, project managers

---

### **→ scripts/README.md**
*Guida agli script di backup e restore*

Contiene:
- Quick start Windows e Linux
- Automazione backup (Task Scheduler, Cron)
- Struttura cartelle
- Troubleshooting

**Tempo di lettura**: 10 minuti
**Per chi**: Chiunque faccia backup regolari

---

### **→ BACKUP_FILES_SUMMARY.md**
*Elenco completo di tutti i file creati*

Contiene:
- Descrizione di ogni file
- Flusso di lavoro
- Struttura backup
- Configurazione tecnica
- Quando usare ogni script

**Tempo di lettura**: 15 minuti
**Per chi**: Chi vuole capire tutto nei dettagli

---

## 🔧 Script di Automazione

### Windows (`.bat` files)

```
scripts/
├── start-local.bat          → Avvia app
├── backup-local.bat         → Backup dati
└── restore-local.bat        → Ripristina da backup
```

**Uso**: Doppio-click o da cmd

---

### Linux/Mac (`.sh` files)

```
scripts/
├── start-local.sh           → Avvia app
├── backup-local.sh          → Backup dati
└── restore-local.sh         → Ripristina da backup
```

**Uso**: Rendi eseguibili e lancia da terminale

```bash
chmod +x scripts/*.sh
./scripts/start-local.sh
```

---

## 📁 Struttura di Progetto

```
gestionale-edile/
├── 📄 QUICK_START.md              ← LEGGI PRIMA
├── 📄 LOCAL_SETUP.md
├── 📄 DEPLOYMENT_LOCAL.md
├── 📄 BACKUP_FILES_SUMMARY.md
├── 📄 THIS_FILE (INDEX.md)
│
├── scripts/                        ← Automazione
│   ├── README.md
│   ├── start-local.bat/.sh
│   ├── backup-local.bat/.sh
│   └── restore-local.bat/.sh
│
├── db/                             ← Database (creato al primo avvio)
│   └── gestionale-edile.h2.db
│
├── backups/                        ← Archive di backup (creato con script)
│   ├── gestionale-edile-20241028-1425.zip
│   └── gestionale-edile-20241021-2200.zip
│
├── pom.xml                         ← Build config (Maven)
├── application.properties          ← Config app (H2 locale)
│
├── src/main/java/                  ← Codice backend Java
│   └── com/edile/
│
├── src/main/resources/static/      ← Frontend HTML/CSS/JS
│   ├── index.html
│   ├── anagrafica-personale.html
│   ├── nuovo-dipendente.html
│   ├── carica-bolla-materiali.html
│   └── assets/
│       └── styles.css
│
└── target/                         ← Build output (ignorare)
    ├── gestionale-edile-1.0.0.jar  ← App eseguibile
    └── (altri file compilazione)
```

---

## 🎯 Roadmap di Lettura Consigliate

### Scenario 1: "Voglio solo usare l'app"
1. ✅ QUICK_START.md (5 min)
2. ✅ Scripts: start-local.bat (doppio-click)
3. ✅ Accedi: admin/admin

**Totale**: 10 minuti, app running

---

### Scenario 2: "Voglio capire come funziona"
1. ✅ QUICK_START.md (5 min)
2. ✅ LOCAL_SETUP.md (10 min)
3. ✅ DEPLOYMENT_LOCAL.md (15 min)
4. ✅ scripts/README.md (10 min)

**Totale**: 40 minuti, expert knowledge

---

### Scenario 3: "Voglio fare backup regolari"
1. ✅ scripts/README.md (10 min)
2. ✅ Automazione con Task Scheduler (5 min)
3. ✅ Prova: scripts\backup-local.bat (2 min)

**Totale**: 17 minuti, backup automatico

---

### Scenario 4: "Voglio tutto in dettaglio"
1. ✅ QUICK_START.md (5 min)
2. ✅ LOCAL_SETUP.md (10 min)
3. ✅ DEPLOYMENT_LOCAL.md (15 min)
4. ✅ scripts/README.md (10 min)
5. ✅ BACKUP_FILES_SUMMARY.md (15 min)

**Totale**: 55 minuti, master knowledge

---

## 🔍 Domande Frequenti

### D: Come faccio a iniziare?
**R**: Leggi QUICK_START.md (5 min), poi esegui `scripts\start-local.bat`

### D: Cosa succede se perdo i dati?
**R**: Se hai fatto backup (scripts\backup-local.bat), puoi ripristinare con `scripts\restore-local.bat`

### D: Posso accedere da altro PC?
**R**: Sì, da altro PC sulla stessa rete: `http://192.168.1.100:8080/api/` (cambia IP)

### D: Quanto spazio occupa?
**R**: Database: 5-50MB, Backup: 2-10MB ciascuno

### D: Posso fare backup automatici?
**R**: Sì, vedi scripts/README.md (automazione con Task Scheduler / Cron)

### D: Quali sono i requisiti minimi?
**R**: Java 8+, 512MB RAM, 1GB disco libero

### D: Posso ospitare online?
**R**: Sì, vedi "Prossimi Step" in DEPLOYMENT_LOCAL.md (Render.com o Aruba VPS)

---

## ✨ Prossimi Step

### Fase 1: Avvio (Oggi)
- [x] Crea file di backup
- [ ] Avvia `scripts\start-local.bat`
- [ ] Accedi: admin/admin
- [ ] Fai primo backup

### Fase 2: Utilizzo Regolare (Questa Settimana)
- [ ] Inserisci dati iniziali
- [ ] Fai backup settimanale
- [ ] Testa tutte le funzioni

### Fase 3: Automazione (Opzionale)
- [ ] Imposta backup automatico (Task Scheduler)
- [ ] Crea script di monitoring
- [ ] Documenta procedure

### Fase 4: Hosting Online (Futuro)
- [ ] Decidi tra Render.com o Aruba VPS
- [ ] Migra da H2 a PostgreSQL
- [ ] Deploy online
- [ ] Continua con backup cloud

---

## 💡 Tips & Tricks

1. **Primo avvio**: È normale che sia lento (scarica dipendenze)
2. **Backup**: Fai almeno backup settimanali
3. **Recupero**: Se qualcosa va male, ripristina da backup (script provided)
4. **Monitoraggio**: H2 console a http://localhost:8080/api/h2-console (opzionale)
5. **Performance**: Per singolo utente, H2 è perfetto. Se crescono gli utenti, migrare a PostgreSQL

---

## 📞 Supporto

Se qualcosa non funziona:

1. **Leggi** il troubleshooting nella guida correlata
2. **Prova** a ripulire e ricompilare: `mvn clean package`
3. **Ripristina** da backup se i dati sono corrotti
4. **Controlla** che Java 8+ sia installato: `java -version`

---

## 🎁 File Bonus

Questi file sono stati creati per completezza:

- `pom.xml` - Configurazione Maven (JAR + H2)
- `application.properties` - Config app (già pronto per H2 locale)
- `QUICK_START.md` - Guida 5 minuti
- `LOCAL_SETUP.md` - Setup dettagliato
- `DEPLOYMENT_LOCAL.md` - Strategie deploy
- `BACKUP_FILES_SUMMARY.md` - Riepilogo completo
- `scripts/README.md` - Guida agli script
- `scripts/*.bat` - Script Windows
- `scripts/*.sh` - Script Linux/Mac

---

## ✅ Status: Pronto per Production (Local)

- ✅ Build Maven validata
- ✅ Database H2 configurato
- ✅ Script backup/restore pronti
- ✅ Documentazione completa
- ✅ Guida quick start
- ✅ Troubleshooting incluso

**Prossimo passo**: Leggi QUICK_START.md

---

**Data**: Ottobre 2024  
**Versione**: 1.0.0  
**Stato**: Ready for deployment  
**Deployable**: ✅ YES (Local H2)

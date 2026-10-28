# 🎯 MAPPA VISUALE DELLA DOCUMENTAZIONE

```
┌─────────────────────────────────────────────────────────────────┐
│                  GESTIONALE EDILE - DOCUMENTAZIONE             │
│                    (Deployment Locale H2)                       │
└─────────────────────────────────────────────────────────────────┘

                            📖 LEGGI PRIMA
                                  │
                                  ▼
                        ┌──────────────────┐
                        │   INDEX.md       │ ⭐
                        │  (Mappa Completa)│
                        └─────────┬────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
                    ▼             ▼             ▼
          ┌────────────────┐  ┌─────────────┐  ┌──────────────┐
          │ QUICK_START.md │  │ LOCAL_..    │  │ DEPLOYMENT..│
          │                │  │ SETUP.md    │  │ LOCAL.md     │
          │ ⏱️  5 min      │  │             │  │              │
          │ 🚀 Start Here  │  │ ⏱️  10 min   │  │ ⏱️  15 min   │
          │                │  │ 🔧 Technical │ │ 🎯 Decisions │
          └────────────────┘  └─────────────┘  └──────────────┘
                    │             │             │
                    └─────────────┼─────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  BACKUP_FILES_SUMMARY.md │
                    │  (Riepilogo Completo)    │
                    │  ⏱️  15 min             │
                    │  📋 Tutti i dettagli    │
                    └──────────────────────────┘
```

---

## 🎓 LIVELLI DI LETTURA

### 🟢 VERDE - "Voglio solo usare l'app" (10 min)
```
QUICK_START.md 
    ↓
scripts\start-local.bat
    ↓
admin / admin
    ↓
✅ App ready!
```

### 🔵 BLU - "Voglio capire come funziona" (40 min)
```
QUICK_START.md
    ↓
LOCAL_SETUP.md
    ↓
DEPLOYMENT_LOCAL.md
    ↓
scripts/README.md
    ↓
✅ Expert knowledge
```

### 🔴 ROSSO - "Voglio tutto in dettaglio" (55 min)
```
QUICK_START.md
    ↓
LOCAL_SETUP.md
    ↓
DEPLOYMENT_LOCAL.md
    ↓
scripts/README.md
    ↓
BACKUP_FILES_SUMMARY.md
    ↓
✅ Master knowledge
```

---

## 📚 STRUTTURA DOCUMENTAZIONE

```
ROOT
├─ 📄 README_DEPLOYMENT.txt (Questo file)
│
├─ 🗺️  MAPPA DELLA DOCUMENTAZIONE
│  ├─ INDEX.md ⭐ ← LEGGI PRIMA
│  │
│  ├─ Per iniziare velocemente:
│  │  └─ QUICK_START.md
│  │
│  ├─ Per capire la configurazione:
│  │  ├─ LOCAL_SETUP.md
│  │  └─ DEPLOYMENT_LOCAL.md
│  │
│  ├─ Per dettagli completi:
│  │  ├─ BACKUP_FILES_SUMMARY.md
│  │  └─ scripts/README.md
│  │
│  └─ Script di automazione:
│     └─ scripts/
│        ├─ *.bat (Windows)
│        ├─ *.sh (Linux/Mac)
│        └─ README.md
│
└─ 🔨 PROGETTO
   ├─ pom.xml (Build Maven)
   ├─ application.properties (Config H2)
   ├─ src/ (Codice)
   ├─ db/ (Database)
   └─ backups/ (Backup)
```

---

## 🎯 COME USARE QUESTA MAPPA

### Se sei qui la PRIMA volta:
1. Leggi questo file (che stai leggendo)
2. Vai a **INDEX.md**
3. Leggi **QUICK_START.md**
4. Esegui `scripts\start-local.bat`
5. Accedi: admin/admin

### Se vuoi imparare di più:
1. Leggi **LOCAL_SETUP.md** (configurazione)
2. Leggi **DEPLOYMENT_LOCAL.md** (opzioni deploy)
3. Leggi **scripts/README.md** (automazione backup)

### Se vuoi master knowledge:
1. Leggi tutto nell'ordine: INDEX.md → QUICK_START.md → LOCAL_SETUP.md → DEPLOYMENT_LOCAL.md → BACKUP_FILES_SUMMARY.md

### Se hai un problema:
1. Leggi **QUICK_START.md** sezione "Se Qualcosa Non Funziona"
2. Vedi **LOCAL_SETUP.md** sezione "Troubleshooting"
3. Vedi **DEPLOYMENT_LOCAL.md** sezione "Troubleshooting Rapido"

---

## 📍 LANDMARK IMPORTANTI

| Cosa cerchi? | Dove trovarla? |
|-------------|-----------------|
| Come avviare? | QUICK_START.md |
| Configurazione H2? | LOCAL_SETUP.md |
| Opzioni deploy? | DEPLOYMENT_LOCAL.md |
| Script backup? | scripts/README.md |
| Errore durante avvio? | QUICK_START.md (sezione Troubleshooting) |
| Errore database? | DEPLOYMENT_LOCAL.md (Disaster Recovery) |
| Automazione backup? | scripts/README.md (Automazione Backup) |
| Struttura cartelle? | LOCAL_SETUP.md o BACKUP_FILES_SUMMARY.md |
| Primo accesso? | QUICK_START.md (sezione "Dopo l'Avvio") |

---

## ✨ FILE CHIAVE

### 1️⃣ **INDEX.md** (ESSENZIALE)
   - Indice completo
   - Roadmap di lettura
   - Domande frequenti
   - Status progetto
   
   👉 **INIZIA DA QUI**

### 2️⃣ **QUICK_START.md** (RAPIDO)
   - Avvio in 30 secondi
   - Primo accesso
   - Primo backup
   - Troubleshooting veloce
   
   👉 **SE FRETTI**

### 3️⃣ **LOCAL_SETUP.md** (TECNICO)
   - Requisiti sistema
   - Comandi build
   - Struttura progetto
   - Accesso H2 console
   
   👉 **SE VUOI DETTAGLI**

### 4️⃣ **DEPLOYMENT_LOCAL.md** (STRATEGICO)
   - Decisioni architetturali
   - 3 Opzioni di deploy
   - Sicurezza e backup
   - Quando migrare a cloud
   
   👉 **SE DECIDI LA STRATEGIA**

### 5️⃣ **BACKUP_FILES_SUMMARY.md** (COMPLETO)
   - Elenco tutti i file
   - Configurazione tecnica
   - Flusso di lavoro
   - Checklist finale
   
   👉 **SE VUOI MASTER KNOWLEDGE**

### 6️⃣ **scripts/README.md** (AUTOMATION)
   - Guida agli script
   - Automazione backup (Task Scheduler)
   - Automazione backup (Cron)
   - Troubleshooting
   
   👉 **SE VUOI BACKUP AUTOMATICI**

---

## 🚀 QUICK COMMAND REFERENCE

```bash
# AVVIO
scripts\start-local.bat              # Windows
./scripts/start-local.sh             # Linux/Mac

# BACKUP
scripts\backup-local.bat             # Windows
./scripts/backup-local.sh            # Linux/Mac

# RESTORE
scripts\restore-local.bat BACKUP.zip # Windows
./scripts/restore-local.sh BACKUP.tar.gz # Linux/Mac

# BUILD MANUALE
mvn clean package
mvn spring-boot:run

# ACCESSO
http://localhost:8080/api/           # App
http://localhost:8080/api/h2-console # H2 Console
```

---

## 💡 CONSIGLI VELOCI

1. **Prima volta?** → Leggi QUICK_START.md (5 min)
2. **Vuoi backup?** → Esegui `scripts\backup-local.bat` dopo ogni sessione
3. **Errore?** → Vedi troubleshooting in QUICK_START.md
4. **Molti dati?** → Fai backup settimanali (automazione in scripts/README.md)
5. **Crescita?** → Quando server più utenti, vedi "Prossimi Step" in DEPLOYMENT_LOCAL.md

---

## ✅ CHECKLIST PRIMO GIORNO

- [ ] Leggi QUICK_START.md (5 min)
- [ ] Esegui `scripts\start-local.bat`
- [ ] Accedi: http://localhost:8080/api/
- [ ] Username: admin
- [ ] Password: admin
- [ ] Fai primo backup: `scripts\backup-local.bat`
- [ ] Prova inserire dati
- [ ] Prova tutte le pagine
- [ ] Fai secondo backup

**Totale tempo**: 30 minuti
**Risultato**: App fullly operational ✅

---

## 🎁 BONUS

**Tutti questi file sono gratuiti e pronti all'uso:**

✅ 13 file di automazione (script)
✅ 6 guide complete (documentazione)
✅ Database H2 embedded (gratuito)
✅ Server Tomcat embedded (gratuito)
✅ Build Maven (gratuito)
✅ Zero costi di hosting (locale)

**Total**: 16 file ready-to-deploy 🚀

---

## 🎯 PROSSIMO PASSO

👉 **Apri: INDEX.md** (mappa principale)

oppure

👉 **Apri: QUICK_START.md** (se fretti)

---

**Versione**: 1.0.0
**Data**: Ottobre 2024
**Status**: ✅ Ready for production (local deployment)

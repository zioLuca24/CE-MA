# Gestionale Edile - Backup & Deploy

Struttura per mantenere il progetto sempre deployabile in locale con H2.

## 📋 Contenuto

```
scripts/
├── start-local.bat          # Windows: Build e avvio applicazione
├── start-local.sh           # Linux/Mac: Build e avvio applicazione
├── backup-local.bat         # Windows: Backup del database e configurazione
├── backup-local.sh          # Linux/Mac: Backup del database e configurazione
├── restore-local.bat        # Windows: Ripristino da backup
└── restore-local.sh         # Linux/Mac: Ripristino da backup

backups/                      # Cartella per i backup (auto-creata)
└── gestionale-edile-YYYYMMDD-HHMMSS.zip/.tar.gz

LOCAL_SETUP.md               # Guida completa setup locale
```

## 🚀 Quick Start (Windows)

```bash
# 1. Avvia l'applicazione
scripts\start-local.bat

# 2. Accedi
# http://localhost:8080/api/
# admin / admin

# 3. Fai un backup
scripts\backup-local.bat

# 4. Se necessario, ripristina
scripts\restore-local.bat backups\gestionale-edile-YYYYMMDD-HHMMSS.zip
```

## 🚀 Quick Start (Linux/Mac)

```bash
# 1. Rendi eseguibili gli script
chmod +x scripts/*.sh

# 2. Avvia l'applicazione
./scripts/start-local.sh

# 3. Accedi
# http://localhost:8080/api/
# admin / admin

# 4. Fai un backup
./scripts/backup-local.sh

# 5. Se necessario, ripristina
./scripts/restore-local.sh ./backups/gestionale-edile-YYYYMMDD-HHMMSS.tar.gz
```

## 💾 Backup Automatico

Puoi schedulare backup quotidiani:

### Windows (Task Scheduler)
```
Azione: Esegui programma
Programma: %CD%\scripts\backup-local.bat
Frequenza: Giornaliera
Ora: 22:00 (dopo chiusura lavoro)
```

### Linux/Mac (Cron)
```bash
# Aggiungi al crontab (crontab -e)
0 22 * * * cd /path/to/gestionale-edile && ./scripts/backup-local.sh
```

## 📁 Cartelle importanti

- `db/` - Database H2 (creato al primo avvio)
- `backups/` - Tutti i backup ZIP
- `src/main/resources/static/` - Frontend HTML/CSS/JS

## ⚠️ Note importanti

1. **Database locale gratuito**: H2 è completamente gratuito e embedded
2. **Persistenza**: Il database rimane sempre disponibile in `db/gestionale-edile.h2.db`
3. **Backup**: Includi sempre il file H2 negli script di backup
4. **Restore**: Prima di ripristinare, arresta l'applicazione
5. **File size**: Un backup completo è tipicamente 2-10 MB

## 🔍 Troubleshooting

### L'applicazione non parte?
```bash
# Prova a pulire la cache di Maven
mvn clean

# Poi riavvia
scripts\start-local.bat
```

### Il database è corrotto?
```bash
# Elimina il database
rm db/gestionale-edile.h2.db*

# Ripristina da backup
scripts\restore-local.bat backups\ultimo-backup.zip
```

### Port 8080 già in uso?
Modifica `application.properties`:
```properties
server.port=8081
```

## 📚 Ulteriori informazioni

Vedi `LOCAL_SETUP.md` per dettagli completi di setup, URL console H2, e struttura del progetto.

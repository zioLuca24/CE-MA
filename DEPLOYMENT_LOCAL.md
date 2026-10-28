# DEPLOYMENT LOCALE - Guida Completa

## ✅ Configurazione Attuale

Il progetto è **completamente configurato per deployment locale** con:
- ✅ Database H2 embedded (gratuito, zero config)
- ✅ JAR con Tomcat embedded (nessun application server esterno)
- ✅ Scripts pronti per backup/restore
- ✅ Build Maven ottimizzato

## 🎯 Opzioni di Deploy

### Opzione 1: Deploy su PC locale (CONSIGLIATO)
**Migliore per**: Uso quotidiano, sviluppo, testing

```bash
# Windows
scripts\start-local.bat

# Linux/Mac
./scripts/start-local.sh
```

**Vantaggi**:
- ✅ Nessun costo
- ✅ Accesso immediato (http://localhost:8080/api/)
- ✅ Backup semplice con script
- ✅ Database H2 persistente e gratuito
- ✅ Massima privacy: tutto rimane locale

### Opzione 2: Deploy su PC diverso (Network)
**Migliore per**: Condivisione tra dispositivi in ufficio

```bash
# 1. Da PC A (server): avvia come prima
scripts\start-local.bat

# 2. Da PC B (client): accedi a
http://<IP_DI_PC_A>:8080/api/
```

**Nota**: Entrambi devono essere sulla stessa rete

### Opzione 3: Deploy USB portatile
**Migliore per**: Lavoro in cantiere, offline

```bash
# 1. Copia su USB:
#    - gestionale-edile/
#    - scripts/
#    - db/
#    - target/

# 2. Su qualsiasi PC con Java 8+:
cd E:\gestionale-edile  # (o lettera USB)
scripts\start-local.bat
```

## 📋 Checklist Pre-Deploy

Prima di mettere online, verifica:

- [ ] Build passa: `mvn clean package`
- [ ] Database H2 esiste: `db/gestionale-edile.h2.db`
- [ ] Frontend carica: http://localhost:8080/api/
- [ ] Login funziona: admin / admin
- [ ] Tutte le pagine rispondono:
  - [ ] Cantieri
  - [ ] Personale
  - [ ] Materiali
  - [ ] Fornitori
- [ ] Ultimo backup fatto: `scripts/backup-local.bat`
- [ ] Disco ha spazio libero (>500MB)

## 🔒 Sicurezza Deploy Locale

### Backup Settimanali
```bash
# Mantieni almeno 4 backup recenti
scripts\backup-local.bat
# Sposta i vecchi backup su disco esterno se lo spazio è limitato
```

### Protezione File Database
```bash
# Windows: fai dei backup periodici su disco esterno
# Linux/Mac: puoi usare rsync per backup automatico
```

### Accesso Remoto (se necessario)
Se vuoi accedere da **altro PC** sulla rete:

1. Identifica IP del PC server: `ipconfig` (Windows) o `ifconfig` (Linux)
2. Accedi da altro PC: `http://192.168.1.100:8080/api/` (esempio)

⚠️ **ATTENZIONE**: Accesso remoto senza autenticazione forte espone i dati. Se necessario accesso remoto, aggiungi:
- VPN privata
- Firewall
- Autenticazione più forte

## 🚨 Disaster Recovery

### Se il database si corrompe:
```bash
# 1. Arresta applicazione
# 2. Ripristina da backup
scripts\restore-local.bat backups\gestionale-edile-ultimadata.zip

# 3. Riavvia
scripts\start-local.bat
```

### Se perdi il file H2:
```bash
# Se hai backup:
scripts\restore-local.bat backups\backup-recente.zip

# Se non hai backup:
# Il database si ricrea vuoto al prossimo avvio
# Ma tutti i dati vengono persi!
# ⚠️ Fai backup settimanali!
```

## 📊 Monitoraggio

### Verificare che tutto funzioni
```bash
# L'app è avviata se vedi:
# "Tomcat initialized with port(s): 8080 (http)"
# "Started GestionaleEdileApplication"

# H2 console disponibile a:
# http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:file:./db/gestionale-edile;MODE=Oracle
```

### Log file
I log vengono stampati in console durante l'esecuzione. Per salvare i log:
```bash
# Windows PowerShell
.\scripts\start-local.bat | Tee-Object -FilePath logs.txt

# Linux/Mac
./scripts/start-local.sh | tee logs.txt
```

## 💻 Requisiti Tecnici Minimi

- **RAM**: 512 MB disponibili
- **CPU**: Qualsiasi (anche Raspberry Pi va bene)
- **Disco**: 1 GB libero
- **Java**: JRE 8 o superiore (gratuito)
- **Maven** (solo per build): 3.6+

## ✨ Prossimi Step

Quando sarai pronto per il vero hosting online (non locale):

1. **Render.com** (consigliato, gratuito):
   - Copia progetto su GitHub
   - Connetti GitHub a Render
   - Deploy con PostgreSQL (configurazione già pronta)

2. **Aruba VPS** (più costoso ma custom):
   - Affittare VPS
   - SSH e deploy manuale
   - Gestire server

**Per ora**: Usa il deployment locale con script di backup. È completamente gratuito, veloce e affidabile per uso quotidiano.

## 📞 Troubleshooting Rapido

| Problema | Soluzione |
|----------|-----------|
| "Port 8080 already in use" | Modifica `application.properties`: `server.port=8081` |
| "Database locked" | Arresta app, aspetta 5 sec, riavvia |
| "OutOfMemory" | Incrementa RAM: `java -Xmx512m -jar ...` |
| "File not found" | Assicurati di avviare da directory root gestionale-edile |
| "Authentication failed" | Controlla credenziali: admin / admin |

---

**Data aggiornamento**: 2024
**Versione**: 1.0.0
**Stato**: Ready for production (local deployment)

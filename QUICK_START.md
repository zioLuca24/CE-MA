# 🚀 QUICK START - Gestionale Edile Locale

## Opzione 1: Avvio Veloce (CONSIGLIATO)

Doppio click su:
```
scripts\start-local.bat
```

Si aprirà una finestra che:
1. Compila il progetto
2. Avvia l'applicazione
3. Mostra l'URL di accesso

**Poi accedi a**: http://localhost:8080/api/
**Login**: admin / admin

---

## Opzione 2: Da Riga di Comando

```bash
# Vai nella cartella
cd C:\Users\ludemart\OneDrive - Capgemini\Desktop\CE.MA\gestionale-edile

# Avvia
scripts\start-local.bat
```

---

## Opzione 3: Se usi Maven direttamente

```bash
mvn spring-boot:run
```

---

## ⏱️ Primo Avvio (Richiede 1-2 minuti)

La prima volta è più lenta perché:
1. Maven scarica tutte le dipendenze (60MB circa)
2. Compila il codice
3. Crea il database H2

**Avvii successivi**: molto più veloce (10-20 secondi)

---

## 📌 Dopo l'Avvio

```
✅ Vedi questo? Significa che funziona:

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 
2024-10-28 14:45:30.123  INFO 1234 --- [main] c.e.GestionaleEdileApplication : 
Starting GestionaleEdileApplication v1.0.0
...
2024-10-28 14:45:35.456  INFO 1234 --- [main] o.s.b.w.e.tomcat.TomcatWebServer  : 
Tomcat initialized with port(s): 8080 (http)

2024-10-28 14:45:37.890  INFO 1234 --- [main] c.e.GestionaleEdileApplication : 
Started GestionaleEdileApplication in 7.567 seconds
```

**Apri browser**: http://localhost:8080/api/

---

## 💾 Primo Backup

Dopo le prime modifiche/inserimenti, fai un backup:

```bash
scripts\backup-local.bat
```

Si creerà: `backups\gestionale-edile-20241028-1425.zip`

---

## 🔐 Accesso H2 Console (opzionale)

Se vuoi vedere direttamente il database:

1. Vai a: http://localhost:8080/api/h2-console
2. JDBC URL: `jdbc:h2:file:./db/gestionale-edile;MODE=Oracle`
3. User: `sa`
4. Password: (vuota)
5. Connect

---

## ⚠️ Se Qualcosa Non Funziona

### L'app non si avvia?
```bash
# Prova a pulire la cache
mvn clean
# Poi riavvia
scripts\start-local.bat
```

### "Port 8080 already in use"?
```bash
# Modifica il file:
# src/main/resources/application.properties
# Cambia: server.port=8081
```

### Il database è corrotto?
```bash
# Elimina il file
rm db/gestionale-edile.h2.db

# Oppure ripristina da backup
scripts\restore-local.bat backups\ultimo-backup.zip
```

---

## 📚 Ulteriori Guide

- **LOCAL_SETUP.md** - Configurazione dettagliata
- **DEPLOYMENT_LOCAL.md** - Opzioni di deploy (USB, Network, ecc)
- **scripts/README.md** - Guida agli script
- **BACKUP_FILES_SUMMARY.md** - Riepilogo di tutti i file creati

---

## ✨ Pronto?

Esegui adesso:
```
scripts\start-local.bat
```

E accedi a:
```
http://localhost:8080/api/
admin / admin
```

Buon lavoro! 🎉

# 🚀 Guida Deployment Render - Gestionale Edile

**Creato:** Ottobre 2024  
**Versione:** 1.0.0  
**Database:** PostgreSQL (Free Tier)  
**Server:** Render.com Web Service (Free Tier)  

---

## 📋 Pre-requisiti

✅ Account GitHub con il repository collegato  
✅ Account Render.com (https://render.com)  
✅ Accesso admin al repository

---

## 🎯 Step 1: Creare il Database PostgreSQL su Render

### 1.1 Accedi a Render Dashboard
1. Vai su https://dashboard.render.com
2. Accedi con GitHub

### 1.2 Crea Database PostgreSQL
1. Clicca su **"+ New"** → **"PostgreSQL"**
2. **Nome database:** `gestionale-edile-db`
3. **Database name:** `gestionale_edile_prod`
4. **User:** `gestionale_user`
5. **Regione:** Scegli la più vicina (ES: Frankfurt per Europa)
6. **Version:** PostgreSQL 15 (default)
7. Clicca su **"Create Database"**

### 1.3 Copia le credenziali
Dopo la creazione, visualizzerai una schermata con:
- **Database:** `gestionale_edile_prod`
- **User:** `gestionale_user`
- **Password:** (salvala in luogo sicuro!)
- **Host:** `dpg-xxxxxx.render.internal`
- **External Database URL:** `postgresql://gestionale_user:PASSWORD@dpg-xxxxxx.render.com:5432/gestionale_edile_prod`

**IMPORTANTE:** Salva l'**External Database URL** completo - ti servirà nel prossimo step!

---

## 🎯 Step 2: Creare il Web Service su Render

### 2.1 Vai al Dashboard Render
1. Clicca su **"+ New"** → **"Web Service"**

### 2.2 Configura il Repository
1. **Connetti il tuo Repository GitHub**
   - Se non è collegato, clicca "Connect GitHub Account"
   - Autorizza Render ad accedere ai tuoi repository
2. **Seleziona il repository:** `gestionale-edile` (o il nome del tuo)
3. **Branch:** `main` (o il branch di default)

### 2.3 Configurazione Base
1. **Nome:** `gestionale-edile`
2. **Regione:** Stessa del database (ES: Frankfurt)
3. **Runtime:** Java
4. **Build Command:** 
   ```
   mvn clean package -DskipTests -Prender
   ```
5. **Start Command:** 
   ```
   java -jar target/gestionale-edile-1.0.0.jar
   ```
6. **Piano:** Free (gratuito)

### 2.4 Configurazione Avanzata
1. Scrollare giù fino a **"Advanced"**
2. **Health Check Path:** `/api/`
3. **Health Check Protocol:** `HTTP`

---

## 🎯 Step 3: Configurare le Environment Variables

### 3.1 Nel modulo Web Service (stesso schermo)
1. Scrolla fino a **"Environment"**
2. Aggiungi le seguenti variabili:

| Variabile | Valore |
|-----------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `postgresql://gestionale_user:PASSWORD@dpg-xxxxxx.render.com:5432/gestionale_edile_prod` |
| `DB_USER` | `gestionale_user` |
| `DB_PASSWORD` | `(la password del database)` |
| `PORT` | `8080` |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` |

**ATTENZIONE:** Sostituisci:
- `PASSWORD` con la password reale del database
- `dpg-xxxxxx` con l'host reale del tuo database
- L'intera **DATABASE_URL** con quella che hai copiato da Render

### 3.2 Salva le variabili
Clicca su **"Save Changes"**

---

## 🎯 Step 4: Deploy

### 4.1 Avvia il Deploy
1. Clicca su **"Deploy"** o **"Create Web Service"** (dipende dalla schermata)
2. Render inizierà il build automatico:
   - ✅ Checkout del codice
   - ✅ Maven build
   - ✅ Deploy del JAR
   - ✅ Avvio dell'applicazione

### 4.2 Monitora il Deploy
1. Vedrai un log in tempo reale
2. Aspetta fino a quando vedi: **"The application is running at https://gestionale-edile.onrender.com"**

### 4.3 Primo Accesso
1. Apri il browser: **https://gestionale-edile.onrender.com/api/**
2. Login con: **admin / admin**
3. Se vedi la dashboard = ✅ SUCCESSO!

---

## 📋 URL di Produzione

| Servizio | URL |
|----------|-----|
| **Dashboard** | https://gestionale-edile.onrender.com/api/ |
| **Cantieri** | https://gestionale-edile.onrender.com/api/cantieri.html |
| **Personale** | https://gestionale-edile.onrender.com/api/personale.html |
| **Materiali** | https://gestionale-edile.onrender.com/api/materiali.html |
| **Fornitori** | https://gestionale-edile.onrender.com/api/fornitori.html |

---

## ⚙️ Gestione Post-Deploy

### Database
- **Accesso:** Non è necessario accedere direttamente
- **Backup:** Render crea backup giornalieri automatici (free tier: 7 giorni)
- **Ripristino:** Nel dashboard Render → Database → Backups

### Logs
- **Accesso:** Render Dashboard → Web Service → Logs
- **Troubleshooting:** Leggi i log se trovi errori

### Restart Manuale
1. Render Dashboard → Web Service `gestionale-edile`
2. Clicca su **"Manual Deploy"** → **"Clear Build Cache & Deploy"**

### Aggiornamenti
- Push su GitHub → Render auto-deploy automatico
- Aspetta ~2 minuti per il rebuild e il restart

---

## 🔐 Sicurezza

### HTTPS Automatico
✅ Render fornisce HTTPS gratuito automatico  
✅ Certificati SSL rinnovati automaticamente  

### Password Admin
L'applicazione usa username/password HTTP Basic:
- **Default:** admin / admin
- **Modifica:** (implementare endpoint di cambio password nel backend se necessario)

### Database
- Database interno a Render (non esposto pubblicamente)
- Usa role-based access control PostgreSQL
- Backup automatico ogni 24 ore

---

## 🚨 Troubleshooting

### Build Failed
**Problema:** Build non riesce  
**Soluzione:**
1. Verifica che Maven build funzioni in locale: `mvn clean package -DskipTests`
2. Controlla i log Render per l'errore specifico
3. Assicurati che la versione Java sia 8+

### Database Connection Error
**Problema:** "Connection refused" o errore di connessione  
**Soluzione:**
1. Verifica che DATABASE_URL sia corretto
2. Controlla che il database PostgreSQL sia stato creato
3. Attendi 2-3 minuti dopo la creazione del database (initializzazione)
4. Nel Render Dashboard, vai al database e verifica Status = "Available"

### Applicazione lenta/timeout
**Problema:** App carica lentamente o va in timeout  
**Soluzione:**
1. Primo accesso è sempre lentissimo (cold start ~30-60 sec)
2. Accesso successivi sono veloci (10-20 sec)
3. Se persiste: aumenta JAVA_OPTS memory (ma limita a 512MB su free tier)

### Port già in uso
**Problema:** "Port 8080 already in use"  
**Soluzione:**
- Render gestisce i port automaticamente, non cambiare PORT a mano
- Se succede, fare Manual Deploy da Render Dashboard

---

## 📊 Limiti Free Tier Render

| Risorsa | Limite |
|---------|--------|
| **Web Service** | 1 vCPU, 512MB RAM |
| **Database** | 100MB storage |
| **Backup** | 7 giorni |
| **Banda** | Illimitata |
| **Costo** | $0/mese |

**Note:**
- Se inattivo per 15+ giorni, l'applicazione va in sleep
- Primo accesso dopo sleep = ~30 sec di caricamento
- Per produzione con più utenti: upgrade a PRO ($7/mese)

---

## ✅ Checklist Pre-Production

- [ ] Database PostgreSQL creato
- [ ] Web Service creato
- [ ] Tutte le environment variables configurate
- [ ] Deploy completato con successo
- [ ] Login funziona (admin / admin)
- [ ] Cantieri, Personale, Materiali visibili
- [ ] File upload (bolla materiali) funziona
- [ ] Backup database abilitato

---

## 🔗 Risorse Utili

- **Render Docs:** https://docs.render.com
- **Spring Boot on Render:** https://docs.render.com/deploy-spring
- **PostgreSQL Documentation:** https://www.postgresql.org/docs/15/

---

## 🆘 Supporto

Se riscontri problemi:
1. Controlla i log Render Dashboard
2. Verifica DATABASE_URL e credenziali
3. Ripeti il Manual Deploy
4. Contatta Render Support su https://render.com/support

---

**Versione:** 1.0.0  
**Ultimo aggiornamento:** Ottobre 2024  
**Stato:** Pronto per produzione

# 🚀 RENDER SETUP - Guida Rapida (5 Minuti)

**IMPORTANTE:** Se non l'hai ancora fatto, segui questi passaggi PRIMA di cominciare:

## Prerequisiti

1. ✅ Hai un account Render.com connesso via GitHub
2. ✅ Il repository è su GitHub (https://github.com/tuoaccount/gestionale-edile)
3. ✅ Hai i file seguenti in git:
   - `pom.xml` ✅
   - `render.yaml` ✅ (appena creato)
   - `src/main/resources/application-prod.properties` ✅ (appena creato)
   - `Dockerfile` ✅ (opzionale)

---

## 🎯 Fase 1: Creare Database PostgreSQL (2 min)

### Sul Render Dashboard:

1. Clicca **"+ New"** → **"PostgreSQL"**
2. Compila il form:
   - **Database Name:** `gestionale_edile_prod`
   - **User:** `gestionale_user`
   - **Region:** frankfurt (o la tua regione)
3. Clicca **"Create Database"**
4. **ATTENDI** fino a quando il status dice **"Available"** (1-2 min)

### Salva questi dati (li userai nel prossimo step):
```
External Database URL: postgresql://gestionale_user:PASSWORD@dpg-xxxxxx.render.com:5432/gestionale_edile_prod
Password: (SALVA QUI)
```

---

## 🎯 Fase 2: Creare Web Service (2 min)

### Sul Render Dashboard:

1. Clicca **"+ New"** → **"Web Service"**
2. **Connetti il Repository GitHub:**
   - Se è la prima volta, clicca su "Connect GitHub Account"
   - Autorizza Render
3. Seleziona il repository `gestionale-edile`
4. Compila il form:
   - **Name:** `gestionale-edile`
   - **Region:** frankfurt (stesso del database!)
   - **Runtime:** Java
   - **Build Command:**
     ```
     mvn clean package -DskipTests -Prender
     ```
   - **Start Command:**
     ```
     java -jar target/gestionale-edile-1.0.0.jar
     ```
   - **Plan:** Free (gratuito)

---

## 🎯 Fase 3: Configurare Environment Variables (1 min)

**SULLA STESSA SCHERMATA del Web Service:**

1. Scorri fino a **"Environment"**
2. Aggiungi queste variabili (copia l'External Database URL dal database):

```
SPRING_PROFILES_ACTIVE = prod
DATABASE_URL = postgresql://gestionale_user:PASSWORD@dpg-xxxxxx.render.com:5432/gestionale_edile_prod
DB_USER = gestionale_user
DB_PASSWORD = PASSWORD_DAL_DATABASE
PORT = 8080
JAVA_OPTS = -Xmx512m -Xms256m
```

3. Clicca **"Save Changes"** oppure **"Create Web Service"** (dipende)

---

## ⏳ Fase 4: Deploy Automatico

Render inizia il build automaticamente:
- ✅ Scarica il codice da GitHub
- ✅ Esegue `mvn clean package`
- ✅ Crea il JAR
- ✅ Avvia l'applicazione

**ATTENDI** fino a quando vedi:
```
The application is running at https://gestionale-edile.onrender.com
```

---

## ✅ Verifica il Deploy (primo accesso)

Apri nel browser:
```
https://gestionale-edile.onrender.com/api/
```

Login con: **admin** / **admin**

Se vedi la dashboard = ✅ FUNZIONA!

---

## 🔗 URL della Tua Applicazione

| Pagina | URL |
|--------|-----|
| Dashboard | https://gestionale-edile.onrender.com/api/ |
| Cantieri | https://gestionale-edile.onrender.com/api/cantieri.html |
| Personale | https://gestionale-edile.onrender.com/api/personale.html |
| Materiali | https://gestionale-edile.onrender.com/api/materiali.html |
| Fornitori | https://gestionale-edile.onrender.com/api/fornitori.html |

---

## 🚨 Se Qualcosa Non Funziona

### Build Failed
→ Vai su Render Dashboard → Logs → Leggi l'errore  
→ Controlla che il build funzioni in locale: `mvn clean package -DskipTests`

### Database Connection Error
→ Verifica che DATABASE_URL sia **esatto** (copiato dal database Render)  
→ Attendi 2-3 minuti dopo la creazione del database

### Port Error
→ Non cambiare il PORT a mano  
→ Fai "Manual Deploy" dal dashboard Render

---

## 📝 Prossimi Steps

1. **Aggiornamenti futuri:** Push su GitHub → Render auto-deploy automatico
2. **Backup database:** Render lo fa automaticamente (7 giorni free)
3. **Scalare:** Se servono più risorse → upgrade a PRO ($7/mese)

---

## 📖 Documentazione Completa

Per dettagli completi, leggi: `RENDER_DEPLOYMENT.md`

**Buon deploy! 🎉**

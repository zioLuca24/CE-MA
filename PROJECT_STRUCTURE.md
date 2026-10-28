# 📁 Struttura Progetto Render - Riferimento Rapido

## 🎯 File ESSENZIALI per Render (Devono stare in GitHub!)

```
gestionale-edile/
├── pom.xml                           ✅ Maven config (dependencies, build)
├── render.yaml                       ✅ Render auto-deploy config
├── Dockerfile                        ✅ Containerizzazione (opzionale)
├── src/
│   └── main/
│       ├── java/com/edile/           ✅ Codice backend
│       └── resources/
│           ├── application.properties        ✅ Config default (locale)
│           └── application-prod.properties   ✅ Config Render/PostgreSQL
│       └── static/                           ✅ HTML/CSS/JS frontend
└── .gitignore                        ✅ File da escludere
```

## 🚀 File GENERATI (NON devono stare in GitHub)

Questi file vengono creati durante il build, **NON li commitare:**

```
target/                              ← Build output (gitignore ✅)
*.jar                                ← Generato da build
*.class                              ← Compilato
.idea/                               ← IDE cache
db/                                  ← Database locale (gitignore ✅)
backups/                             ← Backup locali (gitignore ✅)
.env                                 ← Credenziali locali (gitignore ✅)
```

## 📋 Checklist: Cosa Commitare su GitHub

Prima di fare push su GitHub:

- [x] `pom.xml` - Dependencies e build configuration
- [x] `render.yaml` - Render deployment config
- [x] `Dockerfile` - Containerization (opzionale)
- [x] `src/` - Tutto il codice Java e frontend
- [x] `application.properties` - Config di default
- [x] `application-prod.properties` - Config produzione
- [x] `.gitignore` - File da escludere
- [ ] `target/` - ❌ NO (gitignore esclude)
- [ ] `.env` - ❌ NO (password locali, gitignore esclude)
- [ ] `db/` - ❌ NO (database locale, gitignore esclude)

## 🔄 Workflow GitHub → Render

### Quando fai `git push origin main`:
1. GitHub riceve il codice
2. Render webhook si attiva
3. Render scarica il repo
4. Render esegue: `mvn clean package -DskipTests -Prender`
5. Render avvia il JAR con le environment variables
6. Applicazione online in 1-2 minuti

**Tutto automatico!** ✨

## 🔐 Password e Credenziali

**IMPORTANTE:** Non mettere mai password nel codice!

### Dove mettere le credenziali:

❌ **NO**
```java
// Non fare mai così!
String password = "mio_segreto_123";
DATABASE_URL = "jdbc:postgresql://...password...";
```

✅ **SI - Environment Variables su Render Dashboard**
```
DATABASE_URL = ${DATABASE_URL}  ← Render Dashboard
DB_PASSWORD = ${DB_PASSWORD}    ← Render Dashboard
```

Nel codice, usa:
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.password=${DB_PASSWORD}
```

## 📱 Accesso al Database da Remoto

### Durante lo sviluppo (local):
```
Database: H2 (in memory o file)
Accesso: localhost:8080/h2-console
Username: sa
Password: (vuota)
```

### In produzione (Render):
```
Database: PostgreSQL su Render
Accesso: NON diretto (interno Render)
Username: gestionale_user
Password: (dalla Render Dashboard)
```

**Non è necessario** accedere direttamente al database! L'app lo fa automaticamente tramite Hibernate.

## 🛠️ Build Locale vs. Render

### Build Locale (development)
```bash
mvn clean package -DskipTests
java -jar target/gestionale-edile-1.0.0.jar --spring.profiles.active=default
```
→ Usa H2 database locale (`./db/gestionale-edile.h2.db`)

### Build Render (production)
```bash
mvn clean package -DskipTests -Prender
java -jar target/gestionale-edile-1.0.0.jar
```
→ Usa PostgreSQL da Render (dalle environment variables)

## 📁 File di Configurazione

| File | Profilo | Database | Uso |
|------|---------|----------|-----|
| `application.properties` | default | H2 locale | Sviluppo locale |
| `application-prod.properties` | prod | PostgreSQL Render | Produzione |

Spring Boot sceglie il file in base a `SPRING_PROFILES_ACTIVE`:
- **Local**: `spring.profiles.active=default` → `application.properties`
- **Render**: `SPRING_PROFILES_ACTIVE=prod` → `application-prod.properties`

## 🔄 Aggiornamenti Futuri

Quando fai modifiche:

```bash
# Modifica il codice
# ...

# Commit e push
git add .
git commit -m "Fix: description of change"
git push origin main
```

Render automaticamente:
1. Scarica il nuovo codice
2. Fa il build
3. Testa la connessione al database
4. Se OK → deploy automatico
5. Se ERROR → rollback (app non viene stoppata)

## 📊 Limiti Free Tier

| Risorsa | Limite |
|---------|--------|
| **Build time** | No limite (gratis) |
| **Build frequency** | Illimitato (auto su push) |
| **RAM app** | 512MB |
| **CPU app** | Shared 1 vCPU |
| **Database storage** | 100MB (PostgreSQL) |
| **Database connections** | 5 simultanee |
| **HTTPS** | Automatico e gratis |

Se superai i limiti:
- App va in pause (dopo 15+ giorni inattivo)
- Database raggiunge 100MB? Upgrade necessario
- Serve più memoria? Upgrade a PRO ($7/mese)

## 🎯 URL di Accesso

| Ambiente | URL | Database |
|----------|-----|----------|
| **Local** | http://localhost:8080/api | H2 |
| **Render** | https://gestionale-edile.onrender.com/api | PostgreSQL |

---

## ✅ Checklist Finale

Prima di fare push su GitHub:

- [ ] Codice compila localmente: `mvn clean package -DskipTests`
- [ ] Ho testato login: admin/admin
- [ ] Ho testato cantieri/personale/materiali
- [ ] `.gitignore` esclude `target/`, `db/`, `.env`
- [ ] Non c'è password nel codice
- [ ] `application-prod.properties` usa `${VARIABILI}`
- [ ] `pom.xml` ha PostgreSQL driver
- [ ] `render.yaml` è presente e corretto

**Tutto OK? Fai il push e Render fa il resto! 🚀**

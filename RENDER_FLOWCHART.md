# 📊 Render Deployment - Visual Flowchart

## 🎯 Da Qui a Deployment Online

```
┌─────────────────────────────────────────────────────────────────┐
│                   GESTIONALE EDILE v1.0.0                       │
│              Pronto per Render Deployment                       │
└─────────────────────────────────────────────────────────────────┘

                          ┌──────────────────┐
                          │  GitHub Repo OK  │
                          │   (push adesso)  │
                          └────────┬─────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 1: Push on GitHub (1 min)                           [30s]   │
├──────────────────────────────────────────────────────────────────┤
│ git add .                                                        │
│ git commit -m "Render deployment config"                         │
│ git push origin main                                             │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 2: Create PostgreSQL Database (2 min)               [2min]  │
├──────────────────────────────────────────────────────────────────┤
│ 1. Go: https://dashboard.render.com                              │
│ 2. Click: "+ New" → "PostgreSQL"                                 │
│ 3. Fill: gestionale_edile_prod / gestionale_user                │
│ 4. SAVE: External Database URL + Password                        │
│ 5. Wait: Status = "Available"                                    │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 3: Create Web Service (2 min)                       [2min]  │
├──────────────────────────────────────────────────────────────────┤
│ 1. Click: "+ New" → "Web Service"                                │
│ 2. Select: gestionale-edile repository                           │
│ 3. Runtime: Java                                                 │
│ 4. Build: mvn clean package -DskipTests -Prender                 │
│ 5. Start: java -jar target/gestionale-edile-1.0.0.jar            │
│ 6. Plan: Free                                                    │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 4: Set Environment Variables (1 min)                [1min]  │
├──────────────────────────────────────────────────────────────────┤
│ SPRING_PROFILES_ACTIVE = prod                                    │
│ DATABASE_URL = postgresql://gestionale_user:PWD@...             │
│ DB_USER = gestionale_user                                        │
│ DB_PASSWORD = (from database)                                    │
│ PORT = 8080                                                      │
│ JAVA_OPTS = -Xmx512m -Xms256m                                    │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 5: Deploy (10-15 min)                              [15min]  │
├──────────────────────────────────────────────────────────────────┤
│ 1. Click: "Create Web Service"                                   │
│ 2. Render does:                                                  │
│    ├─ Download source code                                       │
│    ├─ Run: mvn clean package -DskipTests                         │
│    ├─ Create JAR (94MB)                                          │
│    ├─ Start Tomcat on port 8080                                  │
│    └─ Expose on: https://gestionale-edile.onrender.com           │
│ 3. Wait for: "The application is running at..."                  │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ STEP 6: Verify & Go Live (1 min)                         [1min]  │
├──────────────────────────────────────────────────────────────────┤
│ 1. Open: https://gestionale-edile.onrender.com/api/             │
│ 2. Login: admin / admin                                          │
│ 3. Check:                                                        │
│    ✓ Dashboard visible                                           │
│    ✓ Cantieri page loads                                         │
│    ✓ Personale page loads                                        │
│    ✓ Materiali page loads                                        │
│    ✓ Fornitori page loads                                        │
│    ✓ Upload bolla works                                          │
│    ✓ Database populated                                          │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
         ┌───────────────────┐
         │  🎉 ONLINE! 🚀   │
         │                   │
         │ URL:              │
         │ https://          │
         │ gestionale-edile  │
         │ .onrender.com/api/│
         └───────────────────┘

TOTAL TIME: ~30 minutes (mostly waiting for build)
```

---

## 🎯 Deployment Timeline

| Phase | What | Duration | Who |
|-------|------|----------|-----|
| 1 | Push to GitHub | 1 min | You |
| 2 | Create Database | 2 min | You |
| 3 | Create Web Service | 2 min | You |
| 4 | Set Variables | 1 min | You |
| 5 | Deploy | 10-15 min | Render (automated) |
| 6 | Verify | 1 min | You |
| **TOTAL** | | **~30 min** | |

---

## 🔄 After Deployment - Auto Updates

```
Your Development:
┌──────────────────┐
│  Modify Code     │
│  (local changes) │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  git add .       │
│  git commit      │
│  git push origin │
│  main            │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│ GitHub Webhook Triggers Render Build     │
└────────┬─────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│ Render:                                  │
│ 1. Clone latest code from GitHub         │
│ 2. mvn clean package -DskipTests         │
│ 3. Stop old JAR                          │
│ 4. Start new JAR                         │
│ 5. Health check: wait for ready          │
└────────┬─────────────────────────────────┘
         │
         ▼
    ┌──────────────────┐
    │ ✅ Live Update!  │
    │ No downtime      │
    │ (2-3 minutes)    │
    └──────────────────┘
```

---

## 📊 Infrastructure Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      INTERNET (HTTPS)                       │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────┐
│              RENDER.COM (CDN + Load Balancer)                │
│                  SSL/TLS Termination                         │
└──────────┬──────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────┐
│         WEB SERVICE (Java Application)                       │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Spring Boot 2.7.15 + Tomcat Embedded                  │  │
│  │ Runtime: Java 8                                       │  │
│  │ RAM: 512MB (Free tier)                                │  │
│  │ CPU: Shared 1 vCPU                                    │  │
│  │ Port: 8080 (internal)                                 │  │
│  │                                                        │  │
│  │ Endpoints:                                            │  │
│  │ - GET  /api/                      → Dashboard         │  │
│  │ - GET  /api/cantieri.html         → Cantieri page    │  │
│  │ - GET  /api/personale.html        → Personale page   │  │
│  │ - GET  /api/materiali.html        → Materiali page   │  │
│  │ - GET  /api/fornitori.html        → Fornitori page   │  │
│  │ - POST /api/upload                → Bolla upload     │  │
│  │ - GET  /api/rest/*                → Backend APIs     │  │
│  └────────┬─────────────────────────────────────────────┘  │
└───────────┼──────────────────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────────────────────────────┐
│              PostgreSQL Database                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Database: gestionale_edile_prod                        │  │
│  │ User: gestionale_user                                  │  │
│  │ Storage: 100MB (Free tier)                             │  │
│  │ Connections: 5 max                                     │  │
│  │ Backup: Daily (7 days retention)                       │  │
│  │                                                        │  │
│  │ Tables:                                               │  │
│  │ - cantieri (sites/projects)                            │  │
│  │ - personale (personnel)                                │  │
│  │ - materiale (materials)                                │  │
│  │ - fornitori (suppliers)                                │  │
│  │ - movimenti_materiale (material movements)             │  │
│  │ - dipendenti (employees)                               │  │
│  │ - turni (shifts)                                       │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Points

1. **Zero Downtime Deployment**
   - Render handles graceful shutdown of old version
   - Health checks ensure new version is ready
   - Automatic rollback if build fails

2. **Automatic Scaling**
   - Free tier: OK for 1-5 concurrent users
   - Pro tier: Better performance for 5-50 users
   - Enterprise: Custom infrastructure

3. **Database Persistence**
   - Data persists across app restarts
   - Automatic backups every day
   - Can restore from backup anytime

4. **HTTPS Security**
   - Automatic SSL/TLS certificate
   - Valid for 90 days, auto-renews
   - No extra cost

5. **Cost Structure**
   - Free tier: $0/month (sleeps after 15 days inactive)
   - Pro tier: $7/month (always running)
   - Database included in both tiers

---

## ✅ Pre-Deployment Checklist

Before starting deployment:

```
CODE:
  [ ] pom.xml has PostgreSQL driver
  [ ] application-prod.properties configured
  [ ] render.yaml present and correct
  [ ] Dockerfile present (optional)
  [ ] .gitignore excludes sensitive files
  [ ] All new files committed to GitHub

GITHUB:
  [ ] Repository is on github.com
  [ ] Branch is 'main' or 'master'
  [ ] Latest code is pushed
  [ ] No uncommitted changes

RENDER ACCOUNT:
  [ ] Account created
  [ ] GitHub connected
  [ ] No existing services
  [ ] Ready to create database

LOCAL BUILD:
  [ ] mvn clean package -DskipTests succeeds
  [ ] JAR file is created (94MB)
  [ ] No compilation errors

YOU:
  [ ] Read RENDER_QUICK_START.md
  [ ] Understand the 6 steps
  [ ] Have 30 minutes available
  [ ] Ready to deploy!
```

---

## 🚀 Ready to Deploy?

→ **Start Here:** `RENDER_START_HERE.md`  
→ **Quick Guide:** `RENDER_QUICK_START.md`  
→ **Full Details:** `RENDER_DEPLOYMENT.md`

**Time needed:** 30 minutes  
**Result:** Live application at https://gestionale-edile.onrender.com/api/

**Let's go! 🎉**

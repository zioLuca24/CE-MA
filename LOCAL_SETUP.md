# Setup Locale Gestionale Edile

## Requisiti
- Java 8+
- Maven 3.6+
- Git

## Avvio in locale

### 1. Build del progetto
```bash
cd gestionale-edile
mvn clean package
```

### 2. Avvio del server
```bash
mvn spring-boot:run
```

Oppure dopo build:
```bash
java -jar target/gestionale-edile-1.0.0.jar
```

### 3. Accedi
- URL: http://localhost:8080/api/
- Username: admin
- Password: admin

## Database (H2 in locale)
- File: `./db/gestionale-edile.h2.db`
- Console H2: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:file:./db/gestionale-edile;MODE=Oracle`
  - User: `sa`
  - Password: (vuota)

## Backup e Restore

### Backup automatico
```bash
./scripts/backup-local.sh
```
Crea archivio in `./backups/gestionale-edile-YYYYMMDD-HHMMSS.tar.gz`

### Restore da backup
```bash
./scripts/restore-local.sh ./backups/gestionale-edile-YYYYMMDD-HHMMSS.tar.gz
```

### Backup manuale database
```bash
cp db/gestionale-edile.h2.db backups/gestionale-edile-manual-YYYYMMDD.h2.db
```

## Cartelle importanti
- `db/` - Database H2
- `backups/` - Archivi di backup
- `src/main/resources/static/` - Frontend HTML/CSS/JS
- `src/main/java/com/edile/` - Codice backend

## Note
- Il database H2 è embedded e gratuito
- Perfetto per sviluppo e uso locale
- Tutto persiste nei file locali

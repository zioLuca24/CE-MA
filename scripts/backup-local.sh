#!/bin/bash

BACKUP_DIR="./backups"
DB_DIR="./db"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
BACKUP_FILE="$BACKUP_DIR/gestionale-edile-$TIMESTAMP.tar.gz"

mkdir -p "$BACKUP_DIR"

if [ ! -f "$DB_DIR/gestionale-edile.h2.db" ]; then
    echo "❌ Database non trovato: $DB_DIR/gestionale-edile.h2.db"
    exit 1
fi

echo "🔄 Backup in corso..."
tar -czf "$BACKUP_FILE" \
    "$DB_DIR/gestionale-edile.h2.db" \
    "src/main/resources/static/" \
    "pom.xml" \
    2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Backup completato: $BACKUP_FILE"
    du -h "$BACKUP_FILE"
else
    echo "❌ Errore durante il backup"
    exit 1
fi

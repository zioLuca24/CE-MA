#!/bin/bash

BACKUP_FILE="${1:-.}"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ File backup non trovato: $BACKUP_FILE"
    echo "Uso: ./restore-local.sh ./backups/gestionale-edile-YYYYMMDD-HHMMSS.tar.gz"
    exit 1
fi

echo "⚠️  ATTENZIONE: Questo ripristinerà il backup e sovrascriverà i dati attuali!"
read -p "Sei sicuro? (s/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "❌ Operazione annullata"
    exit 1
fi

echo "🔄 Ripristino dal backup..."
tar -xzf "$BACKUP_FILE" -C .

if [ $? -eq 0 ]; then
    echo "✅ Ripristino completato da: $BACKUP_FILE"
    echo "Riavvia l'applicazione con: mvn spring-boot:run"
else
    echo "❌ Errore durante il ripristino"
    exit 1
fi

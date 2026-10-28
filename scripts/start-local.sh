#!/bin/bash

echo "============================================"
echo "Gestionale Edile - Avvio in locale"
echo "============================================"
echo

if [ ! -f "pom.xml" ]; then
    echo "❌ Errore: pom.xml non trovato"
    echo "Esegui questo script dalla directory root del progetto"
    exit 1
fi

echo "🔨 Cleaning e building..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "❌ Errore durante la build"
    exit 1
fi

echo
echo "============================================"
echo "✅ Build completato! Avvio dell'applicazione..."
echo "============================================"
echo
echo "URL: http://localhost:8080/api/"
echo

java -jar target/gestionale-edile-1.0.0.jar

echo
echo "Applicazione interrotta"

# Stage 1: Builder
FROM maven:3.8.7-eclipse-temurin-8 as builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:8-jre-noble
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-ita tesseract-ocr-eng fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
WORKDIR /app
COPY --from=builder /build/target/gestionale-edile-1.0.0.jar .
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/ || exit 1
CMD ["java", "-Xmx512m", "-jar", "gestionale-edile-1.0.0.jar"]
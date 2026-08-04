# ============================================================
# 🏗️  СТАДИЯ СБОРКИ
# ============================================================

# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 1. Копируем pom.xml и скачиваем зависимости (кешируется)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Копируем код и собираем JAR
COPY src src
RUN mvn clean package -DskipTests

# ============================================================
# 🚀  ФИНАЛЬНЫЙ ОБРАЗ
# ============================================================

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Безопасность: создаем пользователя
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

USER appuser

# Копируем JAR из builder/ стадии сборки
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]

# Опционально: добавить аргументы JVM/ Запуск с оптимизациями для Docker
# ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

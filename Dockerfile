# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Копируем pom.xml отдельно для кеширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src src
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем пользователя
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

USER appuser

# Копируем JAR из стадии сборки
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]

# Опционально: добавить аргументы JVM
# ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

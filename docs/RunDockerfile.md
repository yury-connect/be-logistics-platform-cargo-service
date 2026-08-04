# 🚀 Как будет осуществляться упаковка в образ?
### Процесс сборки Docker образа:

```bash
# В папке с микросервисом (где лежит Dockerfile)
docker build -t logistics-platform-cargo-service:latest .
```

---
### Что происходит по шагам:
```text
Step 1/12 : FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
 --->  ✅ Скачивается образ Maven (300-400 МБ)

Step 2/12 : WORKDIR /app
 --->  ✅ Устанавливается рабочая директория

Step 3/12 : COPY pom.xml .
 --->  ✅ Копируется твой pom.xml

Step 4/12 : RUN mvn dependency:go-offline -B
 --->  ✅ Скачиваются ВСЕ зависимости из Maven Central
      (этот слой кешируется — если pom.xml не менялся, пересборка идет быстро)

Step 5/12 : COPY src src
 --->  ✅ Копируется твой исходный код

Step 6/12 : RUN mvn clean package -DskipTests
 --->  ✅ Собирается JAR-файл (target/*.jar)
      (этот шаг пересобирается при каждом изменении кода)

Step 7/12 : FROM eclipse-temurin:17-jre-alpine
 --->  ✅ Скачивается финальный JRE-образ (~150 МБ)

Step 8/12 : WORKDIR /app
 --->  ✅ Рабочая директория

Step 9/12 : RUN addgroup --system --gid 1001 appgroup && adduser --system --uid 1001 --ingroup appgroup appuser
 --->  ✅ Создаются пользователь и группа

Step 10/12 : USER appuser
 --->  ✅ Переключение на пользователя appuser

Step 11/12 : COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar
 --->  ✅ Копируется JAR из первого образа (builder)

Step 12/12 : ENTRYPOINT ["java", "-jar", "app.jar"]
 --->  ✅ Команда запуска

Successfully built 9a2b3c4d5e6f
Successfully tagged logistics-platform-cargo-service:latest
```

---
### 🎯 Сборка и запуск
```bash
# 1. ВЕРНУТЬСЯ В ПАПКУ CARGO-SERVICE
cd ..\be-logistics-platform-cargo-service
```

```bash
# 2. СОБРАТЬ ОБРАЗ (в папке с Dockerfile)
docker build -t logistics-platform-cargo-service:latest .
```

```bash
# 3. ПРОВЕРИТЬ ОБРАЗ
docker images | findstr logistics-platform-cargo-service
```

```bash
# 4. ПЕРЕЙТИ В ПАПКУ С DOCKER-COMPOSE
cd ..\be-logistics-platform-infrastructure\local
```

```bash
# 5. ЗАПУСТИТЬ ВСЁ
docker-compose up -d
```

```bash
# 6. ПРОВЕРИТЬ СТАТУС
docker-compose ps
```

```bash
# 7. ПОСМОТРЕТЬ ЛОГИ
docker-compose logs cargo-service --tail 50
```

```bash
# 8. ВЕРНУТЬСЯ В ПАПКУ ПРОЕКТА
cd ..\..\be-logistics-platform-cargo-service
```

```bash
# 9. (ОПЦИОНАЛЬНО) ПОСМОТРЕТЬ ЛОГИ В РЕЖИМЕ РЕАЛЬНОГО ВРЕМЕНИ
#docker-compose logs -f cargo-service
```

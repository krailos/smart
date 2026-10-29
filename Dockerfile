# Рецепт образу Smartum: Docker збирає його рядок за рядком (docker build / docker compose build).
# Два етапи: важкий для збірки (Maven + повна JDK) і легкий для запуску (лише JRE + готовий jar).
# Пароль до бази та профіль prod в образ НЕ кладемо — їх дають compose.yaml і .env під час запуску.

# ---------- Етап 1: збірка ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# спочатку лише pom.xml: поки він не змінився, Docker бере бібліотеки з кешу і не завантажує їх знову
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
# тести запускаються локально перед комітом (а згодом — у CI); тут лише збірка jar
RUN mvn -q package -DskipTests

# ---------- Етап 2: запуск ----------
FROM eclipse-temurin:17-jre
# curl — для перевірки здоров'я (compose питає http://localhost:8080/actuator/health)
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
# застосунок працює не від root: якщо в ньому знайдуть дірку, зловмисник не отримає прав на систему
RUN useradd --system --no-create-home app
WORKDIR /app
COPY --from=build /build/target/smart-0.0.1-SNAPSHOT.jar app.jar
# сервер живе за UTC; без цього LocalDate.now() з 00:00 до ~02:00 за Києвом давав би вчорашню дату
ENV TZ=Europe/Kyiv
USER app
# лише підказка "слухаю 8080"; назовні порт не відкривається (назовні — тільки Caddy, 80/443)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

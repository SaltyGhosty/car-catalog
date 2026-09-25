# ---------- Build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Prima solo wrapper + pom (mvnw usa Maven 3.9.11 anche senza .mvn/): il layer delle dipendenze resta in cache se cambia solo il codice
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- Runtime ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
# Utente non-root
RUN groupadd --system app && useradd --system --gid app --uid 10001 app
COPY --from=build /app/target/app.jar app.jar
USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
# Render passa PORT: Spring la legge tramite server.port=${PORT:8080}
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

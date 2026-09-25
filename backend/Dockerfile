# ---------- Build ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Maven installato direttamente (piu' affidabile del wrapper dentro Docker)
ARG MAVEN_VERSION=3.9.11
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
       | tar -xz -C /opt \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn

# Prima solo il pom: il layer delle dipendenze resta in cache se cambia solo il codice
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q package -DskipTests

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

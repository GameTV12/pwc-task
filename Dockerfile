# Build stage: full JDK + Maven wrapper, so no Java is needed on the host.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Resolve dependencies first so they cache as their own layer; source-only
# changes then rebuild in seconds instead of re-downloading everything.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# Runtime stage: slim JRE only.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /build/target/pwc-task-*.jar app.jar
# Baked-in start file so the image also runs without a bind mount; compose
# mounts the repo's data/ over it so refreshes rewrite the committed snapshot.
COPY data data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

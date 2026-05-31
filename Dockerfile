# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the whole multi-module project and build the executable web jar.
COPY . .
RUN mvn -B clean package -DskipTests

# ---- runtime stage ----
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest
WORKDIR /app
# web module's finalName is web-${version}.jar (default artifactId-version)
COPY --from=build /app/web/target/web-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

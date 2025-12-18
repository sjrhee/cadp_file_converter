# Build stage
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

# Copy pom.xml and dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:11-jre-focal
WORKDIR /app

# Copy built JAR and libraries from build stage
COPY --from=build /app/target/cadp-file-converter-1.0-SNAPSHOT.jar ./app.jar
COPY --from=build /app/target/lib ./lib

# Create a volume for data and logs
VOLUME /data
VOLUME /tmp

# Set environment variables for Java
ENV JAVA_OPTS="-Xms256m -Xmx2g"

# Explicitly set entrypoint to use java -jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command shows help
CMD ["--help"]

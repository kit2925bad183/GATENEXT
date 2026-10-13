# syntax=docker/dockerfile:1

# Stage 1: Build Java Application
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/backend

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine AS runner

# Create non-root system group and user
RUN addgroup -S gatenest && adduser -S gatenest -G gatenest

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /workspace/backend/target/gatenest-backend-*.jar /app/gatenest-backend.jar

# Set ownership
RUN chown -R gatenest:gatenest /app

USER gatenest

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/gatenest-backend.jar"]

# =============================================================================
#  ATS System — Production-Ready Multi-Stage Dockerfile (Maven Wrapper)
# =============================================================================

# ─── Stage 1: Build (using JDK 21 Alpine) ─────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven Wrapper configuration and files
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml

# Ensure mvnw has execution permission
RUN chmod +x mvnw

# Download dependencies (Docker cache layer optimization)
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the application
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ─── Stage 2: Runtime (using JRE 21 Alpine) ───────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S atsgroup && adduser -S atsuser -G atsgroup

# Create the resume uploads directory and assign ownership
RUN mkdir -p /app/uploads/resumes && \
    chown -R atsuser:atsgroup /app

# Copy the executable fat JAR from the builder stage
COPY --from=builder --chown=atsuser:atsgroup /build/target/*.jar app.jar

# Switch to the non-root user
USER atsuser

# Expose the application port
EXPOSE 8080

# Health check to monitor application status
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

# JVM flags for optimized container execution
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]

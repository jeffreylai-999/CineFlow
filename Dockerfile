# syntax=docker/dockerfile:1

FROM node:24-bookworm AS frontend
WORKDIR /workspace/frontend
RUN corepack enable
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm build

FROM eclipse-temurin:25-jdk-jammy AS backend
WORKDIR /workspace/backend
COPY backend/ ./
COPY --from=frontend /workspace/frontend/dist /workspace/frontend/dist
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-jammy AS runtime
WORKDIR /app
COPY --from=backend /workspace/backend/build/libs/*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

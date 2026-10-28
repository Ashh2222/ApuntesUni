# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para producción
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Directorio para almacenamiento persistente
RUN mkdir -p /var/apuntes/uploads
VOLUME /var/apuntes/uploads

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_STORAGE_LOCATION=/var/apuntes/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

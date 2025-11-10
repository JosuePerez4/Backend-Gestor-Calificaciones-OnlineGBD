# Etapa 1: Compilación del proyecto
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copiar archivos de Maven (pom.xml y mvnw)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permisos de ejecución al wrapper
RUN chmod +x mvnw

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto y generar el JAR
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen final con solo el runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
COPY --from=builder /build/target/gestorcalificaciones-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


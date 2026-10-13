# Imagen del backend para desplegarlo en la nube (Render). El frontend se despliega aparte (Vercel),
# por eso aquí se empaqueta sin él.

# --- Etapa 1: compilar el .jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
# En Windows el archivo puede quedar con saltos de línea CRLF, que Linux no entiende
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline -Dskip.frontend=true

COPY src src
RUN ./mvnw -q -B package -DskipTests -Dskip.frontend=true

# --- Etapa 2: imagen final, solo con lo necesario para ejecutar ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/university-store-0.0.1-SNAPSHOT.jar app.jar

# La tienda trabaja con la hora de Colombia: de ella dependen la hora de corte y las fechas de envío
ENV TZ=America/Bogota
# Ajustes para servidores con poca memoria (512 MB en el plan gratuito)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
# En la nube no se aceptan las claves de desarrollo: hay que definir JWT_SECRET y ADMIN_PASSWORD
ENV SECURITY_REQUIRE_SECRETS=true
# En la nube no hace falta imprimir cada consulta SQL
ENV SPRING_JPA_SHOW_SQL=false

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

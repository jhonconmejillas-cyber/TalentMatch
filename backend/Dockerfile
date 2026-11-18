# =====================================================================
# Imagen del backend de TalentMatch (multi-etapa).
# Se construye desde la RAIZ del repositorio, porque necesita conf/ y database/:
#
#   docker build -f backend/Dockerfile -t talentmatch-backend .
#   docker run -p 8080:8080 --env-file .env talentmatch-backend
# =====================================================================

# ---------- Etapa 1: compilar con Maven ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Primero solo el pom y el wrapper: Docker reutiliza esta capa (dependencias
# descargadas) mientras el pom no cambie, y las compilaciones siguientes son rapidas.
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY backend/src src
# Las pruebas corren en el CI (necesitan PostgreSQL); aqui solo se empaqueta.
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: ejecutar con solo el JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios para ejecutar la API
RUN useradd --system --uid 1001 talentmatch

COPY --from=build /build/target/talentmatch-*.jar app.jar
# Parametros de negocio (RNF-12) y migraciones de la base de datos
COPY conf/config.example.yaml conf/config.example.yaml
COPY database/migrations database/migrations
COPY database/seeds database/seeds

# conf/config.yaml se puede montar como volumen para cambiar umbrales sin reconstruir la imagen.
ENV TALENTMATCH_CONF_DIR=/app/conf \
    TALENTMATCH_DB_DIR=/app/database \
    FLYWAY_LOCATIONS=filesystem:/app/database/migrations \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

USER talentmatch
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

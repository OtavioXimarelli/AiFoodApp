# ---- Estágio de Build com Java 23 ----

# Começamos com a imagem oficial do Java 23 (JDK completo).
FROM eclipse-temurin:23-jdk AS build

# Define variáveis para a versão do Maven, para facilitar a atualização.
ARG MAVEN_VERSION=3.9.6
ARG MAVEN_URL=https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

# Instala ferramentas necessárias (curl para download) e baixa e instala o Maven.
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL ${MAVEN_URL} -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /usr/share && \
    ln -s /usr/share/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn && \
    rm /tmp/maven.tar.gz && \
    apt-get purge -y --auto-remove curl

# Define o diretório de trabalho.
WORKDIR /app

# Copia o pom.xml e baixa as dependências (para aproveitar o cache do Docker).
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código-fonte.
COPY src ./src

# Compila e empacota a aplicação.
RUN mvn package -DskipTests

# ---- Estágio Final (Produção) com Java 23 ----

# Usamos a imagem JRE (Java Runtime Environment) do Java 23, que é menor e mais segura.
FROM eclipse-temurin:23-jre

# Define o diretório de trabalho.
WORKDIR /app

# Copia APENAS o arquivo .jar gerado no estágio anterior.
COPY --from=build /app/target/AiFoodAPP-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta da aplicação.
EXPOSE 8080

# Comando para iniciar a aplicação.
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM gradle:7.6-jdk17 AS build

WORKDIR /app

# Copiar arquivos de configuração
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Baixar dependências
RUN gradle dependencies --no-daemon

# Copiar o código-fonte
COPY src ./src

# Compilar a aplicação
RUN gradle build --no-daemon

# Imagem final
FROM openjdk:17-slim

WORKDIR /app

# Copiar o JAR da aplicação do estágio de build
COPY --from=build /app/build/libs/*.jar ./app.jar

# Expor a porta 8080
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "app.jar"] 
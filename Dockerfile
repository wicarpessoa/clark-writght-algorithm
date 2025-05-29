FROM gradle:7.6-jdk17-alpine AS build

WORKDIR /app

# Copiar apenas os arquivos de configuração primeiro
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Baixar dependências (etapa separada para aproveitar o cache do Docker)
RUN gradle dependencies --no-daemon

# Copiar o código-fonte
COPY src ./src

# Compilar a aplicação sem executar testes
RUN gradle assemble --no-daemon

# Imagem final menor
FROM openjdk:17-jdk-alpine

WORKDIR /app

# Copiar o JAR da aplicação do estágio de build
COPY --from=build /app/build/libs/*.jar ./app.jar

# Expor a porta 8080
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "app.jar"] 
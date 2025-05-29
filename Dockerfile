FROM openjdk:17-jdk-alpine

WORKDIR /app

# Copiar o JAR pré-compilado
COPY ../build/libs/trabalho-americo-1.0-SNAPSHOT.jar ./app.jar

# Expor a porta 8080
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "app.jar"] 
#!/bin/bash
echo "Compilando o projeto..."
./gradlew clean build

echo "Executando a aplicação..."
java -cp build/classes/kotlin/main:build/libs/* MainKt

# Ou, se preferir usar o arquivo JAR gerado
# java -jar build/libs/trabalho-americo-1.0-SNAPSHOT.jar 
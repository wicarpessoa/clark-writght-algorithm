#!/bin/bash

# Testar GET /strategies - Listar estratégias disponíveis
echo "Testando GET /strategies - Listagem de estratégias..."
curl -X GET http://localhost:8080/strategies
echo -e "\n\n"

# Testar POST /solve - Calcular rotas
echo "Testando POST /solve - Cálculo de rotas..."
curl -X POST -H "Content-Type: application/json" -d @exemplo-payload.json http://localhost:8080/solve
echo -e "\n\n"

# Testar GET /solve - Listar soluções
echo "Testando GET /solve - Listagem de soluções..."
curl -X GET http://localhost:8080/solve
echo -e "\n\n" 
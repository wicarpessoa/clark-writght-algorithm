# Clark-Wright Algorithm Project

Este projeto implementa o algoritmo de Clark-Wright para solução de problemas de roteamento de veículos, com persistência de dados em PostgreSQL.

## Requisitos

- Docker e Docker Compose
- JDK 17 ou superior
- Kotlin

## Como executar

### 1. Iniciar o banco de dados PostgreSQL

```bash
docker-compose up -d
```

Isso iniciará um contêiner PostgreSQL na porta 5430.

### 2. Executar a aplicação

```bash
./gradlew run
```

A aplicação estará disponível em http://localhost:8080

## Endpoints da API

- `GET /strategies` - Lista todas as estratégias de roteamento disponíveis
- `POST /solve` - Calcula rotas com base na estratégia, distribuidor e clientes fornecidos
- `GET /solve` - Lista todas as soluções salvas anteriormente

Todos os endpoints suportam CORS para integração com frontend.

## Fluxo da Aplicação

1. O frontend permite que o usuário:
   - Obtenha a lista de estratégias disponíveis (`GET /strategies`)
   - Marque um centro de distribuição (CD) no mapa
   - Marque clientes (customers) no mapa
   - Selecione uma estratégia de roteamento

2. O frontend envia todos os dados para o backend em uma única requisição (`POST /solve`)
3. O backend calcula a rota, salva a solução e retorna o resultado
4. O frontend exibe as rotas no mapa usando Leaflet
5. O frontend pode listar soluções anteriores usando `GET /solve`

## Integração com Frontend

O backend está preparado para integração com um frontend através de:
- Suporte a CORS em todos os endpoints
- Formato JSON consistente para requisições e respostas
- Rota dedicada para listar estratégias disponíveis

### Exemplo de resposta da rota de estratégias

```json
[
  {
    "name": "clarkwright",
    "description": "Algoritmo de Clark & Wright - Economias"
  },
  {
    "name": "nearestneighbor",
    "description": "Algoritmo do Vizinho Mais Próximo"
  }
]
```

## Exemplo de requisição para cálculo de rotas

```json
{
  "strategyName": "clarkwright",
  "distributor": {
    "position": {
      "x": 0,
      "y": 0
    }
  },
  "customers": [
    {
      "position": {
        "x": 10,
        "y": 10
      }
    },
    {
      "position": {
        "x": 20,
        "y": 20
      }
    },
    {
      "position": {
        "x": 15,
        "y": 30
      }
    }
  ]
}
```

## Exemplo de resposta

```json
[
  {
    "customers": [
      {
        "id": 1,
        "position": {
          "x": 10,
          "y": 10
        }
      },
      {
        "id": 2,
        "position": {
          "x": 20,
          "y": 20
        }
      },
      {
        "id": 3,
        "position": {
          "x": 15,
          "y": 30
        }
      }
    ],
    "path": [
      {
        "x": 0,
        "y": 0
      },
      {
        "x": 10,
        "y": 10
      },
      {
        "x": 20,
        "y": 20
      },
      {
        "x": 15,
        "y": 30
      },
      {
        "x": 0,
        "y": 0
      }
    ]
  }
]
``` 
# API de Agências - Santander

## Visão Geral do Sistema

API responsável pelo cadastro e consulta de agências bancárias, integrada ao ecossistema de Core Banking do Santander. O sistema permite o gerenciamento de informações geográficas das agências e fornece funcionalidades de busca por proximidade para otimizar a experiência do cliente.

### Principais Tecnologias
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória
- **Maven** - Gerenciamento de dependências
- **Swagger/OpenAPI 3** - Documentação da API

## Requisitos de Ambiente

### Versões Mínimas

- Java 17 ou superior
- Maven 3.8 ou superior


## Instalação e Execução

### Desenvolvimento Local

1. **Clone o repositório:**
   ```bash
   git clone <repository-url>
   cd agencia-api
   ```

2. **Configure as variáveis de ambiente:**
   ```bash
   cp .env.example .env
   # Edite o arquivo .env com suas configurações
   ```

3. **Execute a aplicação:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```


### Ambiente de Testes

```bash
# Executar com perfil de teste
mvn spring-boot:run -Dspring.profiles.active=test
```

### Dependências Externas

- **H2 Database**: Iniciado automaticamente com a aplicação
- **Mocks**: Utilizados nos testes unitários (Mockito)

## Endpoints e Contratos

### Documentação

- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Endpoints Principais

#### Cadastrar Agência
```http
POST /desafio/cadastrar
Content-Type: application/json

{
  "posX": 10.0,
  "posY": -5.0
}
```

**Validações:**
- `posX` e `posY` são obrigatórios
- Não é permitido cadastrar agências muito próximas (distância mínima: 1.0 unidade)
- Retorna erro 400 se já existe agência próxima

#### Buscar Agência por ID
```http
GET /desafio/agencias/{id}
```

#### Buscar Agências Próximas
```http
GET /desafio/distancia?posX=-10&posY=5
```

## Testes

### Execução de Testes

```bash
mvn test
```



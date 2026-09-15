# Travel App Backend

API REST desenvolvida em Java e Spring Boot para atender ao ecossistema do Travel App. A aplicacao adota a arquitetura orientada a funcionalidades (Package-by-Feature) e se conecta a uma base de dados PostgreSQL.

## Tecnologias

| Tecnologia | Finalidade |
| :--- | :--- |
| Java 25 | Linguagem principal do projeto |
| Spring Boot 4.x | Framework central da aplicacao |
| Spring Data JPA | Camada de persistencia e repositorios |
| Hibernate | ORM e integracao com PostgreSQL |
| PostgreSQL | Banco de dados relacional com tipos text[] e jsonb |
| Jakarta Validation | Validacao de integridade e dados de entrada |
| Lombok | Reducao de boilerplate de codigo |
| Apache Maven | Gerenciador de dependencias e build |

## Estrutura de Pacotes

A organizacao do projeto segue o padrao Package-by-Feature:

```text
com.devrenanrodrigues.travelapi
│
├── airport          Regras, consultas e rotas de aeroportos
├── destination      Catalogo de destinos turisticos e integracoes
├── weather          Dados meteorologicos vinculados aos destinos
├── favorite         Marcacao de destinos favoritos por usuario
└── comment          Avaliacoes e comentarios dos usuarios
```

## Configuracao de Ambiente

Para executar o projeto, configure as seguintes variaveis no arquivo application.yaml ou no ambiente:

| Variavel | Descricao | Exemplo |
| :--- | :--- | :--- |
| DB_URL | Endereco JDBC do banco de dados | jdbc:postgresql://localhost:5432/seu_banco_postgresql |
| DB_USERNAME | Usuario autenticado do banco | seu_usuario_postgres |
| DB_PASSWORD | Senha do usuario do banco | sua_senha_postgres |

## Execucao Local

### 1. Clonar o repositorio
```bash
git clone https://github.com/devrnrodrigues/travel-app-backend.git
cd travel-app-backend
```

### 2. Iniciar a aplicacao
```bash
./mvnw spring-boot:run
```

A API ficara disponivel em `http://localhost:8080`.

## Catalogo de Endpoints

### Aeroportos

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| POST | `/api/airports` | Cadastra novo aeroporto |
| GET | `/api/airports` | Lista todos os aeroportos |
| GET | `/api/airports/{id}` | Consulta aeroporto por identificador UUID |
| GET | `/api/airports/iata/{iataCode}` | Consulta aeroporto por codigo IATA de 3 letras |
| DELETE | `/api/airports/{id}` | Remove aeroporto por identificador |

### Destinos

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| POST | `/api/destinations` | Cadastra novo destino turistico |
| GET | `/api/destinations` | Lista destinos com suporte a filtros por categoria, cidade ou pais |
| GET | `/api/destinations/{id}` | Consulta detalhes completos do destino |
| DELETE | `/api/destinations/{id}` | Exclui destino da base de dados |

### Clima do Destino

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| PUT | `/api/destinations/{destinationId}/weather` | Cadastra ou atualiza previsao do tempo |
| GET | `/api/destinations/{destinationId}/weather` | Consulta condicoes climaticas atuais |
| DELETE | `/api/destinations/{destinationId}/weather` | Exclui registro de clima |

### Favoritos

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| POST | `/api/favorites/{destinationId}?userId={uuid}` | Adiciona destino aos favoritos |
| GET | `/api/favorites?userId={uuid}` | Lista todos os destinos favoritados do usuario |
| GET | `/api/favorites/{destinationId}/check?userId={uuid}` | Verifica se o destino ja esta favoritado |
| DELETE | `/api/favorites/{destinationId}?userId={uuid}` | Remove destino dos favoritos |

### Comentarios e Avaliacoes

| Metodo | Endpoint | Descricao |
| :--- | :--- | :--- |
| POST | `/api/destinations/{destinationId}/comments` | Publica avaliacao com nota de 1 a 5 |
| GET | `/api/destinations/{destinationId}/comments` | Consulta comentarios e media de notas |
| DELETE | `/api/comments/{id}` | Remove avaliacao informada |

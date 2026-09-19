# Travel App Backend

API REST desenvolvida em Java e Spring Boot para atender ao ecossistema do Travel App. A aplicacao adota a arquitetura orientada a funcionalidades (Package-by-Feature), seguranca stateless com JWT, controle de acesso baseado em funcoes (RBAC), integracoes externas em tempo real e persistencia em banco de dados PostgreSQL.

## Tecnologias

| Tecnologia | Finalidade |
| :--- | :--- |
| Java 25 | Linguagem principal do projeto |
| Spring Boot 4.x | Framework central da aplicacao |
| Spring Security | Controle de autenticacao e autorizacao |
| OAuth2 Resource Server & JWT | Seguranca stateless com tokens de acesso e refresh tokens |
| Google OAuth2 Client | Validacao de login social com Google Sign-In |
| Spring Data JPA / Hibernate | Camada de persistencia e ORM relacional com PostgreSQL |
| PostgreSQL | Banco de dados relacional com suporte a tipos avancados (text[], jsonb) |
| Jakarta Validation | Validacao declarativa de integridade e payloads de entrada |
| Open-Meteo API | Integracao de dados climaticos com cache automatico de 2 horas |
| RapidAPI / Booking.com | Integracao de busca e consulta de voos em tempo real |
| Springdoc OpenAPI / Swagger | Documentacao e experimentacao interativa dos endpoints |
| Lombok | Reducao de boilerplate de codigo |
| Apache Maven | Gerenciador de dependencias e ciclo de build |

## Estrutura de Pacotes

A organizacao do projeto segue o padrao Package-by-Feature:

```text
com.devrenanrodrigues.travelapi
│
├── airport          Regras, consultas e gerenciamento de aeroportos (IATA, coordenadas)
├── auth             Autenticacao (login local, login Google, refresh token e logout)
├── comment          Avaliacoes e comentarios com calculo de media e moderacao
├── config           Configuracoes globais (Spring Security, CORS, WebClient, OpenApi)
├── destination      Catalogo de destinos turisticos com filtros e protecao de integridade
├── exception        Tratamento global e padronizado de erros (GlobalExceptionHandler)
├── favorite         Marcacao de destinos favoritos vinculados ao usuario autenticado
├── flight           Integracao com RapidAPI (Booking.com) para busca de voos
├── security         Utilitarios de seguranca e extracao de claims JWT (SecurityUtils)
├── user             Entidade de usuario, provedores (LOCAL/GOOGLE) e roles (USER/ADMIN)
└── weather          Integracao meteorologica via Open-Meteo com cache no PostgreSQL
```

## Configuracao de Ambiente

Para executar o projeto, configure as seguintes variaveis de ambiente ou utilize o perfil local pre-configurado (`application-local.yaml`):

| Variavel | Descricao | Exemplo / Padrao |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring Boot | `local` |
| `DB_URL` | Endereco JDBC do banco PostgreSQL | `jdbc:postgresql://localhost:5432/travel_app` |
| `DB_USERNAME` | Usuario autenticado do banco | `postgres` |
| `DB_PASSWORD` | Senha do usuario do banco | `postgres` |
| `JWT_SECRET` | Chave secreta HMAC-SHA256 (minimo 256 bits) | `sua-chave-secreta-jwt-longa` |
| `JWT_EXPIRATION_SECONDS` | Tempo de vida do access token em segundos | `86400` (24h) |
| `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` | Tempo de vida do refresh token em dias | `30` |
| `GOOGLE_CLIENT_ID` | Client ID do Google Cloud Console | `seu-client-id.apps.googleusercontent.com` |
| `RAPIDAPI_KEY` | Chave de acesso a API do Booking.com | `sua-chave-rapidapi` |
| `RAPIDAPI_HOST` | Host da API do Booking.com | `booking-com15.p.rapidapi.com` |
| `WEATHER_OPEN_METEO_BASE_URL` | URL base do servico Open-Meteo | `https://api.open-meteo.com` |
| `WEATHER_CACHE_TTL_HOURS` | Tempo de expiracao do cache climatico | `2` |

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

> **Dica para Testes Locais de Login Google:**
> O endpoint `/api/auth/google` aceita tokens simulados sem necessidade de credenciais da Google. Basta enviar `{"idToken": "dev-mock:<seu-email>:<seu-nome>"}` no corpo da requisicao para obter um token JWT valido imediatamente.

## Catalogo de Endpoints

### Autenticacao

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| POST | `/api/auth/register` | Publico | Cria novo usuario com email e senha |
| POST | `/api/auth/login` | Publico | Autentica usuario local e gera tokens |
| POST | `/api/auth/google` | Publico | Login com Google Sign-In ou mock de desenvolvimento |
| POST | `/api/auth/refresh` | Publico | Rotaciona refresh token e devolve novo access token |
| POST | `/api/auth/logout` | Publico | Revoga refresh token no banco de dados |

### Destinos

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations` | Publico | Lista destinos paginados com filtros por categoria e nome |
| GET | `/api/destinations/{id}` | Publico | Consulta detalhes do destino, clima atualizado e avaliacoes |
| POST | `/api/destinations` | ADMIN | Cadastra novo destino turistico |
| PUT | `/api/destinations/{id}` | ADMIN | Atualiza dados do destino |
| DELETE | `/api/destinations/{id}` | ADMIN | Remove destino (bloqueado se houver avaliacoes ou favoritos) |

### Aeroportos

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/airports` | USER, ADMIN | Lista todos os aeroportos cadastrados |
| GET | `/api/airports/{id}` | USER, ADMIN | Consulta aeroporto por identificador UUID |
| GET | `/api/airports/iata/{iataCode}` | USER, ADMIN | Consulta aeroporto por codigo IATA (3 letras) |
| POST | `/api/airports` | ADMIN | Cadastra novo aeroporto |
| PUT | `/api/airports/{id}` | ADMIN | Atualiza dados cadastrais do aeroporto |
| DELETE | `/api/airports/{id}` | ADMIN | Remove aeroporto da base |

### Voos

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/flights/search` | USER, ADMIN | Busca ofertas de voos diretos e com escalas via RapidAPI |
| GET | `/api/flights/search-multi-stops` | USER, ADMIN | Busca ofertas para multiplos trechos/destinos |

### Clima do Destino

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations/{destinationId}/weather` | Publico | Consulta clima (sincroniza com Open-Meteo caso cache expire) |
| PUT | `/api/destinations/{destinationId}/weather` | ADMIN | Forca atualizacao manual de dados meteorologicos |
| DELETE | `/api/destinations/{destinationId}/weather` | ADMIN | Limpa dados meteorologicos em cache |

### Favoritos

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| POST | `/api/favorites/{destinationId}` | USER, ADMIN | Adiciona destino aos favoritos do usuario autenticado |
| GET | `/api/favorites` | USER, ADMIN | Lista destinos favoritados pelo usuario logado |
| GET | `/api/favorites/{destinationId}/check` | USER, ADMIN | Verifica se o destino informado ja esta favoritado |
| DELETE | `/api/favorites/{destinationId}` | USER, ADMIN | Remove destino da lista de favoritos |

### Comentarios e Avaliacoes

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations/{destinationId}/comments` | Publico | Consulta comentarios, notas e media calculada |
| POST | `/api/destinations/{destinationId}/comments` | USER, ADMIN | Publica avaliacao com nota (1 a 5) para o destino |
| PUT | `/api/comments/{id}` | USER, ADMIN | Edita avaliacao (usuario dono da avaliacao ou ADMIN) |
| DELETE | `/api/comments/{id}` | USER, ADMIN | Exclui avaliacao (usuario dono da avaliacao ou ADMIN) |

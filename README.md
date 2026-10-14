# Travel App Backend

API REST desenvolvida em Java 25 e Spring Boot 4 para atender ao ecossistema do aplicativo Travel App. A aplicacao adota arquitetura modular orientada a funcionalidades (package by feature), seguranca sem estado (stateless) com JWT, controle de acesso baseado em papeis (RBAC), rotacao de tokens com deteccao de reuso, limitacao de taxa (rate limiting) nativa por algoritmo token bucket, armazenamento de imagens na nuvem com transacoes compensatorias, integracoes externas em tempo real e persistencia relacional com PostgreSQL.

## Tecnologias e bibliotecas

| Tecnologia | Finalidade |
| :--- | :--- |
| Java 25 | Linguagem principal do projeto |
| Spring Boot 4.1.1 | Framework central da aplicacao |
| Spring Security | Controle de autenticacao, autorizacao e filtros de requisicao |
| OAuth2 Resource Server e JWT | Seguranca sem estado com tokens de acesso e tokens de atualizacao |
| Google OAuth2 Client | Validacao de login social nativo com Google Sign In |
| Spring Data JPA e Hibernate | Camada de persistencia relacional e ORM com PostgreSQL |
| PostgreSQL | Banco de dados relacional com integridade referencial e indices |
| Cloudinary SDK | Processamento, upload e exclusao de imagens na nuvem |
| Jakarta Validation | Validacao declarativa de integridade de dados e formatos de entrada |
| Open Meteo API | Consulta e sincronizacao de dados climaticos com cache automatico |
| RapidAPI e Booking.com | Consulta e cotacao de passagens aereas e aeroportos em tempo real |
| RestClient e ObjectMapper | Cliente HTTP moderno e serializacao JSON |
| Lombok | Reducao de codigo boilerplate em entidades e DTOs |
| Apache Maven | Gerenciador de dependencias e automacao de build |

## Destaques de arquitetura e seguranca

* Politica e validacao rigorosa de senhas: O sistema aplica validador personalizado (PasswordValidator) no cadastro de usuarios, exigindo tamanho entre 8 e 72 caracteres (compativel com o limite seguro do algoritmo BCrypt), proibindo espacos em branco e exigindo obrigatoriamente letras maiusculas, letras minusculas, numeros e caracteres especiais.
* Criptografia e hashing de senhas com BCrypt: As senhas dos usuarios locais nunca sao salvas em texto puro. O framework aplica BCrypt com salt aleatorio antes de persistir no banco de dados PostgreSQL, prevenindo vazamentos de credenciais.
* Hashing de refresh tokens em SHA 256 e rotacao com deteccao de reuso: O refresh token bruto nunca e gravado no banco de dados. A aplicacao gera um token criptograficamente seguro de 64 caracteres, calcula seu hash SHA 256 e armazena exclusivamente esse hash. A cada renovacao, um novo par e gerado. Caso um token revogado seja reutilizado (indicio de roubo de sessao), uma transacao isolada revoga imediatamente todas as sessoes ativas do usuario. Uma rotina agendada (cron diario as 03:00) expurga tokens expirados ou revogados.
* Limitacao de taxa (rate limiting) por token bucket: Filtro nativo implementado com algoritmo de balde de tokens particionado em niveis de criticidade, protegendo a API contra ataques de forca bruta e sobrecarga de servicos externos:
  * Nivel AUTH: limite de 10 requisicoes por minuto para rotas de autenticacao (/api/auth/**).
  * Nivel EXTERNAL: limite de 15 requisicoes por minuto para cotacoes de voos e clima (/api/flights/** e /api/destinations/*/weather/**).
  * Nivel UPLOAD: limite de 20 requisicoes por minuto para envio de fotos (/api/users/avatar, /api/collections/** e comentarios).
  * Nivel GENERAL: limite de 120 requisicoes por minuto para as demais rotas da API.
  * Extracao de IP com suporte a cabecalhos de proxy reverso (X-Forwarded-For e X-Real-IP).
* Transacoes compensatorias para uploads: No envio de fotos para avaliacoes e colecoes pessoais, o processamento de imagens no Cloudinary ocorre fora da transacao do banco (evitando travamento de conexoes). Em caso de falha de persistencia, as imagens recem enviadas sao automaticamente deletadas da nuvem.
* Validacao rigorosa de arquivos de imagem: O servico de armazenamento aceita exclusivamente arquivos no formato WebP (MIME type image/webp e extensao .webp) com tamanho maximo de 5MB por foto.
* Cabecalhos de seguranca HTTP (OWASP): Protecoes configuradas no filtro de seguranca incluindo X-Frame-Options DENY, X-Content-Type-Options nosniff, HSTS (HTTP Strict Transport Security com max age de 1 ano e subdominios) e politica de referrer strict origin when cross origin.
* Cache meteorologico resiliente: O servico de clima armazena as previsoes no PostgreSQL com tempo de vida configuravel (TTL) e retorna os dados cacheados como contingencia suave caso o servico externo passe por instabilidade.
* Metadados turisticos com suporte a inteligencia artificial: A entidade de destinos possui estrutura preparada para enriquecimento automatizado por IA, com controle de status (PENDING, PROCESSING, COMPLETED, FAILED), resumos contextuais e estimativas de custo em formato jsonb.
* Tratamento centralizado de excecoes: Manipulador global de erros (GlobalExceptionHandler) que higieniza respostas de erro, mapeia falhas de validacao em campos especificos e oculta detalhes internos de infraestrutura atraves de um DTO padronizado.

## Estrutura de pacotes

A organizacao do codigo segue a metodologia orientada a dominios de negocio (package by feature):

```text
com.devrenanrodrigues.travelapi
│
├── airport          Consultas, codigos IATA e cadastro de aeroportos
├── auth             Registro, login local, autenticacao com Google, refresh token e logout
├── category         Categorias tematicas de destinos com suporte a filtro de status ativo
├── collection       Colecoes fotograficas pessoais com upload multiplo e foto de capa
├── comment          Avaliacoes com notas (1 a 5), fotos polaroid e contagem de votos uteis
├── config           Configuracoes de seguranca, CORS, Cloudinary, Jackson e codificadores
├── destination      Catalogo de destinos, metadados turisticos, filtros, ordenacao e fotos
├── exception        Tratamento global de excecoes e padronizacao de respostas de erro
├── favorite         Gestao de destinos salvos vinculados ao usuario autenticado com busca
├── flight           Integracao com RapidAPI para busca de voos simples e multiplos trechos
├── ratelimit        Filtro de requisicoes, algoritmo token bucket em 4 niveis e agendamento
├── security         Utilitarios de contexto de seguranca e extracao de identificadores do token
├── storage          Servico de integracao, validacao de arquivos WebP e envio ao Cloudinary
├── user             Dados de usuario, upload de avatar, papeis (USER e ADMIN) e provedores
└── weather          Integracao meteorologica Open Meteo e persistencia de cache climatico
```

## Catalogo de endpoints

<details>
<summary><strong>Autenticacao e sessao</strong> (5 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| POST | `/api/auth/register` | Publico | Cadastro de novo usuario com e-mail e senha |
| POST | `/api/auth/login` | Publico | Autenticacao local e emissao do par de tokens |
| POST | `/api/auth/google` | Publico | Autenticacao social nativa com Google Sign In |
| POST | `/api/auth/refresh` | Publico | Rotacao do refresh token e emissao de novo token de acesso |
| POST | `/api/auth/logout` | Publico | Revogacao de sessao e invalidacao do refresh token |

</details>

<details>
<summary><strong>Destinos turisticos</strong> (5 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations` | Publico | Listagem paginada com filtros por category, name, ordenacao por sortBy e paginacao |
| GET | `/api/destinations/{id}` | Publico | Consulta detalhada do destino com fotos, clima atualizado e avaliacoes |
| POST | `/api/destinations` | ADMIN | Cadastro de novo destino turistico |
| PUT | `/api/destinations/{id}` | ADMIN | Atualizacao de dados cadastrais e metadados do destino |
| DELETE | `/api/destinations/{id}` | ADMIN | Exclusao de destino (bloqueada se houver avaliacoes ou favoritos vinculados) |

</details>

<details>
<summary><strong>Categorias</strong> (5 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/categories` | Publico | Lista categorias cadastradas (filtro opcional activeOnly, padrao true) |
| GET | `/api/categories/{id}` | Publico | Consulta detalhes de uma categoria por identificador UUID |
| POST | `/api/categories` | ADMIN | Cadastro de nova categoria |
| PUT | `/api/categories/{id}` | ADMIN | Atualizacao de dados da categoria |
| DELETE | `/api/categories/{id}` | ADMIN | Exclusao de categoria |

</details>

<details>
<summary><strong>Aeroportos e voos</strong> (8 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/airports` | USER, ADMIN | Lista aeroportos disponiveis com codigos IATA e coordenadas |
| GET | `/api/airports/{id}` | USER, ADMIN | Consulta aeroporto por identificador UUID |
| GET | `/api/airports/iata/{iataCode}` | USER, ADMIN | Consulta aeroporto por codigo IATA de 3 letras |
| POST | `/api/airports` | ADMIN | Cadastro de aeroporto na base |
| PUT | `/api/airports/{id}` | ADMIN | Atualizacao cadastral de aeroporto |
| DELETE | `/api/airports/{id}` | ADMIN | Exclusao de aeroporto |
| GET | `/api/flights/search` | USER, ADMIN | Cotacao de voos diretos e com conexoes via Booking.com |
| GET | `/api/flights/search-multi-stops` | USER, ADMIN | Cotacao de voos com multiplos destinos |

</details>

<details>
<summary><strong>Clima e meteorologia</strong> (3 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations/{id}/weather` | Publico | Consulta clima do destino (sincroniza com Open Meteo se o cache expirar) |
| PUT | `/api/destinations/{id}/weather` | ADMIN | Atualizacao manual de dados meteorologicos |
| DELETE | `/api/destinations/{id}/weather` | ADMIN | Limpeza dos dados de previsao em cache |

</details>

<details>
<summary><strong>Avaliacoes e comentarios</strong> (5 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/destinations/{id}/comments` | Publico | Consulta comentarios, fotos anexadas e media calculada do destino |
| POST | `/api/destinations/{id}/comments` | USER, ADMIN | Publica avaliacao com nota (1 a 5), texto e upload opcional de ate 5 fotos WebP |
| PUT | `/api/comments/{id}` | USER, ADMIN | Edicao de texto, nota e fotos mantidas ou adicionadas (autor ou ADMIN) |
| DELETE | `/api/comments/{id}` | USER, ADMIN | Exclusao de avaliacao e remocao automatica das fotos no Cloudinary |
| POST | `/api/comments/{id}/helpful` | USER, ADMIN | Votacao de utilidade em avaliacao de outro viajante |

</details>

<details>
<summary><strong>Colecoes pessoais de fotos</strong> (9 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/collections` | USER, ADMIN | Lista colecoes e fotos do usuario autenticado |
| GET | `/api/collections/{id}` | USER, ADMIN | Consulta detalhes de uma colecao especifica |
| POST | `/api/collections` | USER, ADMIN | Criacao de colecao com titulo e upload multiplo de fotos WebP |
| PUT | `/api/collections/{id}` | USER, ADMIN | Edicao de titulo da colecao |
| DELETE | `/api/collections/{id}` | USER, ADMIN | Exclusao de colecao e remocao das fotos associadas no Cloudinary |
| POST | `/api/collections/{id}/photos` | USER, ADMIN | Adiciona novas fotos a uma colecao existente |
| PUT | `/api/collections/{id}/photos/{photoId}` | USER, ADMIN | Atualiza a legenda de uma foto da colecao |
| DELETE | `/api/collections/{id}/photos/{photoId}` | USER, ADMIN | Remove foto especifica da colecao e da nuvem |
| PUT | `/api/collections/{id}/photos/{photoId}/cover` | USER, ADMIN | Define uma foto especifica como foto de capa da colecao |

</details>

<details>
<summary><strong>Destinos favoritos</strong> (4 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/favorites` | USER, ADMIN | Lista destinos favoritados pelo usuario com suporte a paginacao e busca |
| POST | `/api/favorites/{destinationId}` | USER, ADMIN | Adiciona destino aos favoritos |
| GET | `/api/favorites/{destinationId}/check` | USER, ADMIN | Verifica se o destino ja foi favoritado pelo usuario |
| DELETE | `/api/favorites/{destinationId}` | USER, ADMIN | Remove destino da lista de favoritos |

</details>

<details>
<summary><strong>Usuario e perfil</strong> (3 endpoints)</summary>

<br />

| Metodo | Endpoint | Acesso | Descricao |
| :--- | :--- | :--- | :--- |
| GET | `/api/users/me` | USER, ADMIN | Retorna dados do perfil do usuario autenticado |
| PUT | `/api/users/profile` | USER, ADMIN | Atualiza dados cadastrais (nome, biografia, nacionalidade). Aceita tambem o alias /api/users/me |
| POST | `/api/users/avatar` | USER, ADMIN | Upload multipart e substituicao da foto de perfil WebP no Cloudinary |

</details>

## Padronizacao de respostas de erro

Todas as excecoes capturadas pela aplicacao retornam o modelo ErrorResponseDTO com estrutura uniforme:

```json
{
  "timestamp": "2026-10-14T03:30:00Z",
  "status": 400,
  "error": "Erro de validacao",
  "message": "Dados informados sao invalidos.",
  "path": "/api/destinations",
  "fieldErrors": {
    "name": "O nome do destino e obrigatorio.",
    "latitude": "A latitude deve ser informada."
  }
}
```

## Configuracao de ambiente

As variaveis de ambiente da aplicacao podem ser configuradas no sistema ou fornecidas atraves do arquivo application-local.yaml:

| Variavel | Descricao | Padrao ou Exemplo |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring Boot | `local` |
| `DB_URL` | Endereco JDBC de conexao com o PostgreSQL | `jdbc:postgresql://localhost:5432/travel_app` |
| `DB_USERNAME` | Usuario de autenticacao do banco | `postgres` |
| `DB_PASSWORD` | Senha de acesso do banco de dados | `postgres` |
| `JWT_SECRET` | Chave secreta HMAC SHA256 (minimo 256 bits) | `sua-chave-secreta-jwt-longa` |
| `JWT_EXPIRATION_SECONDS` | Validade do token de acesso em segundos | `86400` (24 horas) |
| `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` | Validade do refresh token em dias | `30` |
| `GOOGLE_CLIENT_ID` | Identificador de cliente da Google Cloud | `seu-client-id.apps.googleusercontent.com` |
| `CLOUDINARY_CLOUD_NAME` | Nome do ambiente no Cloudinary | `seu-cloud-name` |
| `CLOUDINARY_API_KEY` | Chave de API do Cloudinary | `sua-api-key` |
| `CLOUDINARY_API_SECRET` | Segredo de API do Cloudinary | `seu-api-secret` |
| `RAPIDAPI_BOOKING_URL` | URL base da API de voos Booking.com | `https://booking-com15.p.rapidapi.com` |
| `RAPIDAPI_KEY` | Chave de acesso a API de voos (RapidAPI) | `sua-chave-rapidapi` |
| `RAPIDAPI_HOST` | Host da API de voos na RapidAPI | `booking-com15.p.rapidapi.com` |
| `WEATHER_OPEN_METEO_BASE_URL` | URL base do servico Open Meteo | `https://api.open-meteo.com` |
| `WEATHER_CACHE_TTL_HOURS` | Tempo de expiracao do cache climatico em horas | `2` |

## Execucao local

### Pre-requisitos
* Java Development Kit (JDK 25) instalado
* PostgreSQL em execucao local ou via container
* Git

### 1. Preparar o banco de dados
Crie o banco de dados relacional no PostgreSQL:
```sql
CREATE DATABASE travel_app;
```

### 2. Clonar o repositorio
```bash
git clone https://github.com/devrnrodrigues/travel-app-backend.git
cd travel-app-backend
```

### 3. Iniciar a aplicacao

No Windows:
```bash
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:
```bash
./mvnw spring-boot:run
```

A API estara acessivel em `http://localhost:8080`.

### 4. Compilar e gerar pacote
Para construir o arquivo JAR de producao:

No Windows:
```bash
.\mvnw.cmd clean package
```

No Linux ou macOS:
```bash
./mvnw clean package
```

## Licenca

Este projeto esta sob a licenca MIT.

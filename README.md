<div align="center">

# Travel App API

<p align="center">
  API REST desenvolvida em <strong>Java 25</strong> e <strong>Spring Boot 4</strong> para o ecossistema do aplicativo Travel App, com arquitetura modular, segurança stateless JWT, rate limiting e integração com PostgreSQL e Cloudinary.
</p>

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)](https://cloudinary.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

[![Ver Frontend](https://img.shields.io/badge/VER_FRONTEND-2ea44f?style=for-the-badge&logo=react&logoColor=white)](https://github.com/devrnrodrigues/travel-app-frontend)

</div>

---

## Tecnologias

| Tecnologia | Finalidade |
| :--- | :--- |
| **Java 25** | Linguagem principal do projeto, utilizando recursos modernos da plataforma Java. |
| **Spring Boot 4.1.1** | Framework central para desenvolvimento rápido, injeção de dependências e configuração do servidor HTTP. |
| **Spring Security** | Mecanismo de autenticação, autorização granular e encadeamento de filtros de segurança. |
| **OAuth2 Resource Server & JWT** | Segurança sem estado (*stateless*) com emissão e validação de tokens de acesso e tokens de renovação. |
| **Google API Client** | Validação criptográfica de credenciais e tokens em logins sociais com Google Sign-In. |
| **Spring Data JPA & Hibernate** | Camada de abstração de repositórios, mapeamento objeto-relacional (ORM) e controle de transações. |
| **PostgreSQL** | Banco de dados relacional principal com integridade referencial, consultas otimizadas e índices. |
| **Cloudinary SDK** | Armazenamento na nuvem, processamento, upload e exclusão de fotos de usuários e coleções. |
| **Jakarta Validation** | Validação declarativa de entrada de dados, formatos, restrições e integridade de DTOs. |
| **Open-Meteo API** | Serviço meteorológico externo integrado para consulta climática com persistência em cache. |
| **RapidAPI & Booking.com API** | Consulta e cotação de voos diretos e com conexões, além de busca de aeroportos em tempo real. |
| **RestClient & Jackson** | Cliente HTTP moderno síncrono e motor de serialização/desserialização JSON de alta performance. |
| **Lombok** | Redução de código *boilerplate* através de anotações automáticas de getters, setters, builders e construtores. |
| **Apache Maven** | Gerenciador de dependências e automação de build do ciclo de vida da aplicação. |

---

## Funcionalidades

* **Autenticação e Gestão de Usuários:**
  * Registro de novas contas com verificação de unicidade de e-mail e hash seguro de senha via BCrypt.
  * Login tradicional e autenticação social com Google Sign-In.
  * Rotação segura de refresh tokens com detecção automática de reuso e revogação em cascata de sessões.
  * Consulta e atualização de dados do perfil (nome, biografia, nacionalidade) e upload de avatar.
* **Catálogo de Destinos Turísticos:**
  * Listagem paginada de destinos com suporte a busca textual por nome/cidade e filtros por categorias temáticas.
  * Consulta detalhada com fotos em alta definição, previsão meteorológica e avaliações associadas.
  * Metadados enriquecidos para IA (resumos culturais e estimativas de custo de viagem).
  * Operações de criação, edição e exclusão restritas a administradores (`ROLE_ADMIN`).
* **Categorias Temáticas:**
  * Organização de destinos por ambientes (praias, montanhas, florestas, cidades históricas).
  * Suporte a esquemas de cores e gradientes contextuais para a interface do frontend.
  * Gestão de status ativo/inativo e manutenção restrita a administradores.
* **Aeroportos e Cotação de Voos:**
  * Catálogo de aeroportos mundiais com códigos IATA de 3 letras, cidade, país e coordenadas.
  * Consulta em tempo real de passagens aéreas via integração com Booking.com (RapidAPI), incluindo filtros de cabine, passageiros e escalas.
* **Clima e Previsão Meteorológica:**
  * Consulta automatizada de dados climáticos via Open-Meteo.
  * Armazenamento de previsão em cache relacional no PostgreSQL com tempo de vida (TTL) configurável.
  * Mecanismo de contingência suave que retorna dados em cache caso a API externa sofra instabilidade.
* **Avaliações da Comunidade:**
  * Sistema de notas de 1 a 5 estrelas e comentários detalhados.
  * Upload de fotos anexadas em formato polaroid (até 5 fotos por comentário).
  * Votação de comentários mais úteis por outros viajantes da comunidade.
* **Coleções Fotográficas Pessoais:**
  * Criação e organização de álbuns de viagem pelo próprio usuário.
  * Upload múltiplo de fotos, edição de legendas e definição de foto de capa do álbum.
* **Destinos Favoritos:**
  * Adição, verificação e remoção rápida de destinos vinculados ao perfil do usuário autenticado.

---

## Segurança

* **Criptografia e Hashing de Senhas (BCrypt):**
  * As senhas dos usuários locais nunca são mantidas em texto puro. O sistema aplica o algoritmo BCrypt com geração aleatória de salt antes de gravar no banco de dados.
* **Hashing de Refresh Tokens em SHA-256 e Detecção de Reuso:**
  * O token de atualização bruto (sequência criptográfica aleatória de 64 caracteres) nunca é salvo no banco de dados. Apenas o seu hash **SHA-256** é persistido.
  * A cada renovação de sessão, o par de tokens é invalidado e um novo par é gerado. Caso um token revogado seja reutilizado (indício de roubo de credencial), uma transação isolada revoga imediatamente todas as sessões ativas daquele usuário.
  * Rotina agendada via cron diário (às 03:00) purga tokens expirados ou revogados da base.
* **Rate Limiting Nativo por Token Bucket:**
  * Filtro de requisições implementado com o algoritmo de balde de tokens, com limites segmentados por criticidade:
    * **AUTH:** limite de 10 requisições por minuto (`/api/auth/**`).
    * **EXTERNAL:** limite de 15 requisições por minuto (`/api/flights/**` e clima).
    * **UPLOAD:** limite de 20 requisições por minuto (envio de fotos de avatar, comentários e coleções).
    * **GENERAL:** limite de 120 requisições por minuto para os demais endpoints.
    * Suporte a proxies reversos com leitura dos cabeçalhos `X-Forwarded-For` e `X-Real-IP`.
* **Controle de Acesso Baseado em Papéis (RBAC):**
  * Separação rigorosa entre `ROLE_USER` (operações pessoais e de comunidade) e `ROLE_ADMIN` (gestão do catálogo e infraestrutura).
* **Transações Compensatórias para Uploads:**
  * O processamento e upload de fotos no Cloudinary é executado fora da transação do banco de dados (evitando travamento de conexões). Em caso de falha de persistência no PostgreSQL, uma rotina compensatória remove automaticamente as imagens recém-enviadas da nuvem.
* **Cabecalhos HTTP Recomendados pela OWASP:**
  * Configurações de segurança no filtro de resposta HTTP, incluindo `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Strict-Transport-Security` (HSTS de 1 ano com subdomínios) e `Referrer-Policy: strict-origin-when-cross-origin`.

---

## Validações

* **Política Rigorosa de Senhas (`PasswordValidator`):**
  * O cadastro de usuários exige senhas entre 8 e 72 caracteres (compatível com o limite seguro do BCrypt), proíbe espaços em branco e impõe obrigatoriamente letras maiúsculas, minúsculas, números e caracteres especiais.
* **Validação Estrita de Arquivos de Imagem:**
  * O serviço de armazenamento valida os arquivos recebidos e aceita exclusivamente o formato **WebP** (`image/webp` e extensão `.webp`), limitando o tamanho a no máximo **5MB** por foto.
* **Validação Declarativa nos DTOs (Jakarta Validation):**
  * Uso de anotações `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min` e `@Max` nos pontos de entrada de dados, impedindo payloads inconsistentes.
* **Integridade de Negócio:**
  * Bloqueio de exclusão de destinos que possuam avaliações ou favoritos associados.
  * Validação de unicidade para aeroportos (código IATA único) e e-mails de usuários.

---

## Tratamento de Erros

A aplicação utiliza um manipulador global de exceções (`GlobalExceptionHandler`) baseado em `@RestControllerAdvice`, garantindo que todas as falhas retornem um contrato JSON padronizado (`ErrorResponseDTO`), sem vazar detalhes internos ou stacktraces da infraestrutura:

```json
{
  "timestamp": "2026-11-07T20:30:00Z",
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

* **400 Bad Request:** retornado em falhas de validação sintática ou regras de negócio (com mapa de `fieldErrors`).
* **401 Unauthorized:** credenciais ausentes, inválidas ou token expirado.
* **403 Forbidden:** acesso a recurso restrito a papel superior (ex: rota de administrador).
* **404 Not Found:** identificador de destino, comentário, coleção ou usuário inexistente.
* **429 Too Many Requests:** acionamento do limite de requisições por minuto no filtro de rate limiting.
* **500 Internal Server Error:** mensagens genéricas e seguras voltadas ao cliente, com log detalhado registrado no servidor.

---

## Arquitetura

### Estrutura de pastas

O código-fonte segue a metodologia orientada a domínios de negócio (*package by feature*):

```text
com.devrenanrodrigues.travelapi
│
├── airport          # Consultas, códigos IATA e cadastro de aeroportos
├── auth             # Registro, login local, autenticação com Google, refresh token e logout
├── category         # Categorias temáticas de destinos com suporte a filtro de status ativo
├── collection       # Coleções fotográficas pessoais com upload múltiplo e foto de capa
├── comment          # Avaliações com notas (1 a 5), fotos polaroid e contagem de votos úteis
├── config           # Configurações de segurança, CORS, Cloudinary, Jackson e codificadores
├── destination      # Catálogo de destinos, metadados turísticos, filtros, ordenação e fotos
├── exception        # Tratamento global de exceções e padronização de respostas de erro
├── favorite         # Gestão de destinos salvos vinculados ao usuário autenticado com busca
├── flight           # Integração com RapidAPI para busca de voos simples e múltiplos trechos
├── ratelimit        # Filtro de requisições, algoritmo token bucket em 4 níveis e agendamento
├── security         # Utilitários de contexto de segurança e extração de identificadores do token
├── storage          # Serviço de integração, validação de arquivos WebP e envio ao Cloudinary
├── user             # Dados de usuário, upload de avatar, papéis (USER e ADMIN) e provedores
└── weather          # Integração meteorológica Open-Meteo e persistência de cache climático
```

### Rotas da API

<details>
<summary><strong>Autenticação e Sessão</strong> (5 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Público | Cadastro de novo usuário com e-mail e senha. |
| `POST` | `/api/auth/login` | Público | Autenticação local e emissão do par de tokens (Access e Refresh Token). |
| `POST` | `/api/auth/google` | Público | Autenticação social nativa com Google Sign-In via ID Token. |
| `POST` | `/api/auth/refresh` | Público | Rotação do refresh token e emissão de novo token de acesso. |
| `POST` | `/api/auth/logout` | Público | Revogação de sessão e invalidação do refresh token. |

</details>

<details>
<summary><strong>Destinos Turísticos</strong> (5 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/destinations` | Público | Listagem paginada com busca por nome, filtro de categoria e ordenação. |
| `GET` | `/api/destinations/{id}` | Público | Consulta detalhada do destino com fotos, clima atualizado e avaliações. |
| `POST` | `/api/destinations` | ADMIN | Cadastro de novo destino turístico. |
| `PUT` | `/api/destinations/{id}` | ADMIN | Atualização de dados cadastrais e metadados turísticos do destino. |
| `DELETE` | `/api/destinations/{id}` | ADMIN | Exclusão de destino (bloqueada se houver avaliações ou favoritos associados). |

</details>

<details>
<summary><strong>Categorias</strong> (5 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/categories` | Público | Lista categorias temáticas cadastradas (filtro opcional `activeOnly`). |
| `GET` | `/api/categories/{id}` | Público | Consulta detalhes de uma categoria por identificador UUID. |
| `POST` | `/api/categories` | ADMIN | Cadastro de nova categoria. |
| `PUT` | `/api/categories/{id}` | ADMIN | Atualização de dados e esquemas de cores da categoria. |
| `DELETE` | `/api/categories/{id}` | ADMIN | Exclusão de categoria. |

</details>

<details>
<summary><strong>Aeroportos e Voos</strong> (8 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/airports` | USER, ADMIN | Lista aeroportos disponíveis com códigos IATA e coordenadas. |
| `GET` | `/api/airports/{id}` | USER, ADMIN | Consulta aeroporto por identificador UUID. |
| `GET` | `/api/airports/iata/{iataCode}` | USER, ADMIN | Consulta aeroporto por código IATA de 3 letras. |
| `POST` | `/api/airports` | ADMIN | Cadastro de aeroporto na base. |
| `PUT` | `/api/airports/{id}` | ADMIN | Atualização cadastral de aeroporto. |
| `DELETE` | `/api/airports/{id}` | ADMIN | Exclusão de aeroporto. |
| `GET` | `/api/flights/search` | USER, ADMIN | Cotação de voos diretos e com conexões via Booking.com (RapidAPI). |
| `GET` | `/api/flights/search-multi-stops` | USER, ADMIN | Cotação de voos com múltiplos destinos. |

</details>

<details>
<summary><strong>Clima e Meteorologia</strong> (3 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/destinations/{id}/weather` | Público | Consulta clima do destino (sincroniza com Open-Meteo se o cache expirar). |
| `PUT` | `/api/destinations/{id}/weather` | ADMIN | Atualização manual de dados meteorológicos. |
| `DELETE` | `/api/destinations/{id}/weather` | ADMIN | Limpeza forçada dos dados de previsão em cache. |

</details>

<details>
<summary><strong>Avaliações e Comentários</strong> (5 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/destinations/{id}/comments` | Público | Consulta comentários, fotos anexadas e média calculada do destino. |
| `POST` | `/api/destinations/{id}/comments` | USER, ADMIN | Publica avaliação com nota (1 a 5), texto e upload opcional de até 5 fotos WebP. |
| `PUT` | `/api/comments/{id}` | USER, ADMIN | Edição de texto, nota e fotos mantidas ou adicionadas (autor ou ADMIN). |
| `DELETE` | `/api/comments/{id}` | USER, ADMIN | Exclusão de avaliação e remoção automática das fotos no Cloudinary. |
| `POST` | `/api/comments/{id}/helpful` | USER, ADMIN | Votação de utilidade em avaliação de outro viajante. |

</details>

<details>
<summary><strong>Coleções Pessoais de Fotos</strong> (9 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/collections` | USER, ADMIN | Lista coleções e fotos do usuário autenticado. |
| `GET` | `/api/collections/{id}` | USER, ADMIN | Consulta detalhes de uma coleção específica. |
| `POST` | `/api/collections` | USER, ADMIN | Criação de coleção com título e upload múltiplo de fotos WebP. |
| `PUT` | `/api/collections/{id}` | USER, ADMIN | Edição do título da coleção. |
| `DELETE` | `/api/collections/{id}` | USER, ADMIN | Exclusão de coleção e remoção das fotos associadas no Cloudinary. |
| `POST` | `/api/collections/{id}/photos` | USER, ADMIN | Adiciona novas fotos a uma coleção existente. |
| `PUT` | `/api/collections/{id}/photos/{photoId}` | USER, ADMIN | Atualiza a legenda de uma foto da coleção. |
| `DELETE` | `/api/collections/{id}/photos/{photoId}` | USER, ADMIN | Remove foto específica da coleção e da nuvem. |
| `PUT` | `/api/collections/{id}/photos/{photoId}/cover` | USER, ADMIN | Define uma foto específica como foto de capa da coleção. |

</details>

<details>
<summary><strong>Destinos Favoritos</strong> (4 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/favorites` | USER, ADMIN | Lista destinos favoritados pelo usuário com paginação e busca. |
| `POST` | `/api/favorites/{destinationId}` | USER, ADMIN | Adiciona destino aos favoritos. |
| `GET` | `/api/favorites/{destinationId}/check` | USER, ADMIN | Verifica se o destino já foi favoritado pelo usuário. |
| `DELETE` | `/api/favorites/{destinationId}` | USER, ADMIN | Remove destino da lista de favoritos. |

</details>

<details>
<summary><strong>Usuários e Perfil</strong> (3 endpoints)</summary>

<br />

| Método | Endpoint | Acesso | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | USER, ADMIN | Retorna dados do perfil do usuário autenticado. |
| `PUT` | `/api/users/profile` | USER, ADMIN | Atualiza dados cadastrais (nome, biografia, nacionalidade). Aceita também o alias `/api/users/me`. |
| `POST` | `/api/users/avatar` | USER, ADMIN | Upload multipart e substituição da foto de perfil WebP no Cloudinary. |

</details>

---

## Documentação da API

> [!NOTE]
> A documentação interativa com **Swagger / OpenAPI 3** (`springdoc-openapi-starter-webmvc-ui`) está atualmente em fase de **planejamento e implementação**.
>
> Enquanto isso, todos os 47 endpoints, parâmetros de acesso, payloads e respostas estão documentados diretamente na seção [Rotas da API](#rotas-da-api) deste README.

---

## Configuração de Ambiente

As variáveis de ambiente da aplicação podem ser configuradas no sistema operacional ou fornecidas através de um arquivo `application-local.yaml`:

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring Boot | `local` |
| `DB_URL` | Endereço JDBC de conexão com o PostgreSQL | `jdbc:postgresql://localhost:5432/travel_app` |
| `DB_USERNAME` | Usuário de autenticação do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha de acesso do PostgreSQL | `postgres` |
| `JWT_SECRET` | Chave secreta HMAC-SHA256 (mínimo 256 bits) | `sua-chave-secreta-jwt-longa` |
| `JWT_EXPIRATION_SECONDS` | Validade do token de acesso (em segundos) | `86400` (24 horas) |
| `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` | Validade do refresh token (em dias) | `30` |
| `GOOGLE_CLIENT_ID` | Identificador de cliente OAuth2 na Google Cloud | `seu-client-id.apps.googleusercontent.com` |
| `CLOUDINARY_CLOUD_NAME` | Nome do ambiente no Cloudinary | `seu-cloud-name` |
| `CLOUDINARY_API_KEY` | Chave pública de API do Cloudinary | `sua-api-key` |
| `CLOUDINARY_API_SECRET` | Chave secreta de API do Cloudinary | `seu-api-secret` |
| `RAPIDAPI_BOOKING_URL` | URL base da API de voos Booking.com | `https://booking-com15.p.rapidapi.com` |
| `RAPIDAPI_KEY` | Chave de acesso à API de voos (RapidAPI) | `sua-chave-rapidapi` |
| `RAPIDAPI_HOST` | Host da API de voos na RapidAPI | `booking-com15.p.rapidapi.com` |
| `WEATHER_OPEN_METEO_BASE_URL` | URL base do serviço meteorológico Open-Meteo | `https://api.open-meteo.com` |
| `WEATHER_CACHE_TTL_HOURS` | Tempo de expiração do cache climático (em horas) | `2` |

---

## Testes

> [!NOTE]
> A suíte de testes automatizados do backend está atualmente em fase de **planejamento e estruturação**.

### Escopo e ferramentas planejadas:
* **Testes unitários:** [JUnit 5](https://junit.org/junit5/) e [Mockito](https://site.mockito.org/) para validação isolada de regras de negócio em serviços (`Services`), validações de senha e rate limiting.
* **Testes de integração de endpoints:** [Spring WebMvc Test](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing) com `MockMvc` para os controladores REST e filtros de segurança.
* **Testes de persistência:** `@DataJpaTest` com banco relacional de teste para validação de queries, relacionamentos e transações de banco de dados.

---

## Como Rodar o Projeto

### Pré-requisitos
* [Java Development Kit (JDK 25)](https://jdk.java.net/25/) instalado e configurado no PATH
* [PostgreSQL](https://www.postgresql.org/) em execução localmente ou via container Docker
* [Git](https://git-scm.com/)

### 1. Preparar o banco de dados
Crie o banco de dados no PostgreSQL:
```sql
CREATE DATABASE travel_app;
```

### 2. Clonar o repositório
```bash
git clone https://github.com/devrnrodrigues/travel-app-backend.git
cd travel-app-backend
```

### 3. Configurar as variáveis de ambiente
Defina as variáveis de ambiente necessárias ou configure o arquivo `application-local.yaml` com as suas credenciais do PostgreSQL, Cloudinary e APIs externas conforme a tabela de [Configuração de Ambiente](#configuracao-de-ambiente).

### 4. Iniciar a aplicação

No Windows (PowerShell / Prompt):
```bash
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:
```bash
./mvnw spring-boot:run
```

A API estará em execução e acessível em: `http://localhost:8080`.

### 5. Compilar e gerar pacote de produção
Para construir o arquivo JAR de produção:

No Windows:
```bash
.\mvnw.cmd clean package
```

No Linux ou macOS:
```bash
./mvnw clean package
```

---

## Licença

Este projeto está distribuído sob os termos da licença **MIT**. Para mais detalhes, consulte os termos da [Licença MIT](https://opensource.org/licenses/MIT).

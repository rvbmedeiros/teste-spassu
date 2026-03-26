# Spassu Livros

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=springboot)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5-4FC08D?logo=vuedotjs)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![License](https://img.shields.io/badge/license-MIT-blue)

Cadastro de livros desenvolvido como desafio técnico. Implementa CRUD completo de **Livros**, **Autores** e **Assuntos**, geração de relatório PDF, visualização gráfica dos fluxos de orquestração (FlowCockpit) e leitura centralizada de logs, em arquitetura de cinco camadas com autenticação via Keycloak.

---

## Sumário

- [Aderência ao Desafio](#aderência-ao-desafio)
- [Como Executar](#como-executar)
- [Arquitetura](#arquitetura)
- [Padrões de Projeto](#padrões-de-projeto)
- [Tecnologias e Justificativas](#tecnologias-e-justificativas)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Contexto do Desafio](#contexto-do-desafio)

---

## Aderência ao Desafio

Todos os requisitos obrigatórios foram implementados. A tabela abaixo mapeia cada item do enunciado ao que foi entregue.

### Requisitos obrigatórios

| Requisito | Implementação |
|-----------|---------------|
| ✅ CRUD de Livro, Autor e Assunto | Controllers + UseCases + Repositories em arquitetura hexagonal (`microservice`) |
| ✅ Modelo de banco seguido integralmente | `LivroEntity`, `AutorEntity`, `AssuntoEntity` com tipos e tamanhos exatos; tabelas de join explícitas (`LivroAutorEntity`, `LivroAssuntoEntity`) com `@EmbeddedId` |
| ✅ Campo Valor (R$) adicionado ao Livro | `valor DECIMAL(10,2)` — exibido formatado em `pt-BR` via `Intl.NumberFormat` |
| ✅ Interface web com CSS (cor e tamanho) | Tailwind CSS v4 com design tokens (`--ui-brand`, `--ui-bg`, etc.) + dark mode |
| ✅ Formatação de campos (moeda) | `Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })` em `LivrosView` |
| ✅ Relatório com componente de relatórios | JasperReports (.jrxml + .jasper) — exportado como PDF via endpoint |
| ✅ Consulta do relatório via VIEW no banco | `vw_relatorio_livros` criada automaticamente no bootstrap Docker |
| ✅ Relatório agrupa por autor, inclui 3 tabelas | Template com `groupHeader` por `autorNome`; join de `livro`, `autor` e `assunto` via view |
| ✅ Tela inicial com menu/links | `TheNavbar.vue` com rota para todas as seções |
| ✅ Tratamento de erros específico | `ProblemDetail` RFC 9457 — handlers distintos para 404/409/422; microservice sem handler genérico |
| ✅ Scripts de implantação | `docker compose up -d --build` — único comando, ambiente completo |
| ✅ **TDD** *(diferencial)* | ~28 testes frontend (Vitest) + ~28 testes backend (JUnit 5 + Mockito BDD) |

### Diferenciais além do escopo

| Diferencial | Descrição |
|-------------|-----------|
| 🏗️ Arquitetura de 5 camadas | Frontend → BFF → Orchestration → Microservice → PostgreSQL |
| 🔐 Autenticação Keycloak | OAuth2 JWT validado no BFF; dois perfis: `ADMIN` e `USER` (somente leitura) |
| 📊 FlowCockpit | Visualização gráfica BPM-like dos fluxos de orquestração definidos em Java com `@FlowDefinition`/`@FlowStep` |
| 📜 Logs centralizados | Agregação de logs das 3 camadas backend com filtros por `source`, `level`, `search` e polling em tempo real |
| 🐇 RabbitMQ RPC + Eventos | Comunicação síncrona Orchestration → Microservice via request-reply; eventos assíncronos (`created`/`updated`/`deleted`) |
| 🔒 HashiCorp Vault | Todos os secrets (DB, RabbitMQ, Keycloak) centralizados fora da configuração |
| 🌍 i18n | Suporte a `pt-BR` e `en` via `vue-i18n` v10 |
| 🌙 Dark mode | Togglável via `useUiStore`; tokens CSS em `main.css` |
| 🔭 Observabilidade | Logs centralizados com `InMemoryLogStore` + Logback appender customizado (sem dependência externa); AsyncAPI docs via Springwolf |

---

## Como Executar

**Pré-requisito:** Docker e Docker Compose instalados.

```bash
docker compose up -d --build
```

Aguarde todos os serviços ficarem saudáveis (~2–3 min na primeira execução). A ordem de inicialização é gerenciada via `depends_on` + `healthcheck`.

### Acessos

| Serviço | URL | Credencial |
|---------|-----|------------|
| **Frontend** | http://localhost:3000 | `admin / admin123` |
| **Swagger — Orchestration** | http://localhost:8082/swagger-ui.html | — |
| **AsyncAPI UI — Microservice** | http://localhost:8081/springwolf/asyncapi-ui.html | — |
| **Keycloak Admin** | http://localhost:8080 | `kcadmin / admin@2026!` |
| **RabbitMQ Console** | http://localhost:15672 | `spassu / spassu` |
| **Vault** | http://localhost:8200 | Token: `spassu-dev-token` |

> Usuário somente leitura: `usuario / usuario123` (role USER — apenas consulta e relatório).
>
> As credenciais de infraestrutura são definidas no arquivo `.env` na raiz do projeto.

---

## Arquitetura

```
Frontend (Vue 3)  :3000
  └─► BFF (WebFlux)  :8083              — gateway; validação JWT; proxy
        └─► Orchestration (WebFlux)  :8082  — orquestração de fluxos (FlowCockpit)
              └─► Microservice (Spring MVC)  :8081  — regras de negócio (DDD/Hexagonal)
                    └─► PostgreSQL  :5432
```

**Serviços de suporte:** Keycloak `:8080` | RabbitMQ `:5672/:15672` | Vault `:8200`

### Responsabilidades por camada

| Camada | Faz | Não faz |
|--------|-----|---------|
| **BFF** | Valida JWT, normaliza erros, proxy `/api/**` e `/ws/flows` | Regras de fluxo, regras de negócio |
| **Orchestration** | Orquestra fluxos com `@FlowDefinition/@FlowStep`; FlowCockpit WebSocket | Persistência, validação de negócio |
| **Microservice** | Entidades, UseCases, JPA, JasperReports, RabbitMQ publish | Controle de fluxo, autenticação |
| **Frontend** | MVVM via Pinia; componentes Vue reativos | Lógica de negócio, chamadas diretas ao microservice |

### FlowCockpit

Visualização gráfica BPM-like dos workflows de negócio sem exigir leitura de código Java. Annotation scan em startup (`@FlowDefinition`/`@FlowStep`) gera um grafo estático servido via `GET /flows`; execuções em tempo real chegam por WebSocket em `/ws/flows`.

Abordagem escolhida em vez de Camunda/Operaton: o requisito é **legibilidade e documentação**, não engine BPM stateful — sem timers, user tasks ou histórico de instância. A simplicidade operacional é mantida e a decisão é revisável quando surgirem necessidades mais complexas.

### Logs centralizados

Cada módulo backend mantém um `InMemoryLogStore` (max 600 entradas, FIFO) alimentado por um Logback appender customizado. O BFF agrega as três fontes via `RemoteLogsClient` e expõe `GET /api/logs` com filtros: `source`, `level`, `limit`, `search`. O frontend faz polling a cada 5 segundos.

---

## Padrões de Projeto

Os padrões abaixo foram adotados conscientemente dentro do escopo do desafio.

| Padrão | Onde se aplica | Como é aplicado |
|--------|----------------|-----------------|
| **DDD** | `microservice` | Pacotes `domain/model` (POJOs puros), `domain/repository` (interfaces Java), `domain/event` (records imutáveis) e `application/usecase`; zero imports de framework no domínio |
| **Clean Architecture** | `microservice` | Arquitetura hexagonal: portas (interfaces de repositório e UseCases) completamente isoladas dos adaptadores (JPA, Controllers, Messaging) |
| **SOLID** | Todo o backend | **S** — cada UseCase/Controller tem responsabilidade única; **O** — domínio fechado para modificação via interfaces; **L** — implementações trocáveis sem alterar o domínio; **I** — repositórios segregados por entidade; **D** — domínio depende de abstrações, nunca de infraestrutura |
| **DRY** | Todo o projeto | MapStruct elimina código de mapeamento repetido em compile-time; Lombok elimina boilerplate em DTOs/entities; `extractApiErrorMessage` centraliza o parse de erros no frontend |
| **MVVM** | `frontend` | Pinia stores são o único ViewModel; componentes Vue são Views puras — sem chamadas HTTP diretas nos componentes |
| **YAGNI** | Todo o projeto | Sem abstrações especulativas; paginação na UI usa page fixo enquanto não há demanda real de navegação; sem cache layer sem evidência de gargalo |

---

## Tecnologias e Justificativas

### Backend

| Tecnologia | Versão | Por quê |
|------------|--------|---------|
| **Java** | 25 | Versão mais recente; ganhos de performance e recursos modernos da linguagem |
| **Spring Boot** | 4.0.4 | Versão mais recente; auto-configuração madura, ecossistema completo |
| **Spring WebFlux** | — | Reativo/não-bloqueante para Orchestration e BFF; melhor throughput com I/O intensivo |
| **Spring Data JPA + Hibernate** | — | ORM padrão de mercado; reduz boilerplate de persistência no microservice |
| **Spring AMQP (RabbitMQ RPC)** | — | Integração síncrona request-reply e listeners assíncronos sem expor REST interno público |
| **PostgreSQL** | 16-alpine | Banco relacional robusto; suporte a views SQL; usado também pelo Keycloak |
| **Spring Cloud Vault** | — | Centraliza secrets fora do código; todos os módulos consomem via `spring.config.import` |
| **RabbitMQ** | 4-management | RPC síncrono (Orchestration → Microservice) + eventos assíncronos (created/updated/deleted) |
| **Keycloak** | 26.5.1 | Identity Provider; emite JWTs validados pelo BFF via OAuth2 Resource Server |
| **MapStruct** | 1.6.3 | Geração de código de mapeamento em compile-time; zero reflexão em runtime |
| **Lombok** | 1.18.38 | Elimina boilerplate (getters, builders, construtores) |
| **JasperReports** | 7.0.1 | Biblioteca madura de relatórios PDF; alimentada pela view SQL `vw_relatorio_livros` |
| **SpringDoc OpenAPI** | 3.0.0 | Documentação Swagger UI do módulo orchestration |
| **Springwolf** | 2.0.0 | Documentação AsyncAPI para consumers/producers RabbitMQ no microservice |

### Frontend

| Tecnologia | Versão | Por quê |
|------------|--------|---------|
| **Vue 3** | 3.5.x | Framework progressivo; Composition API + `<script setup>` produz código conciso e testável |
| **Vite** | 7.x | Build tool ultrarrápido; HMR instantâneo em desenvolvimento |
| **TypeScript** | — | Tipagem estática elimina classes inteiras de bug; melhor DX com autocomplete |
| **Tailwind CSS** | 4.x | Utilitário CSS com design tokens via CSS custom properties; dark mode nativo |
| **Pinia** | — | State management oficial Vue; implementa a camada ViewModel do padrão MVVM |
| **vue-i18n** | 10.x | Internacionalização `pt-BR`/`en` com Composition API |
| **Axios** | — | Cliente HTTP com interceptors para injeção de token e tratamento global de erros |
| **lucide-vue-next** | — | Ícones SVG leves e tree-shakeable, consistentes com o design system |
| **Vitest** | — | Test runner nativo Vite; configuração zero; API compatível com Jest |

### Infraestrutura

| Tecnologia | Por quê |
|------------|---------|
| **Docker + Docker Compose** | Ambiente reproduzível; `docker compose up -d --build` entrega tudo funcional |
| **nginx** | Serve o frontend compilado (SPA); proxy reverso para o BFF |

---

## Testes

Abordagem TDD-first em todas as camadas, com cobertura mínima de 80%. Testes de backend são unidades isoladas — nenhum Spring context é carregado sem necessidade.

### Backend — JUnit 5 + Mockito

```bash
mvn test          # todos os módulos
mvn test -pl microservice
mvn test -pl orchestration
mvn test -pl bff
```

Convenção: `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`; BDD style `given(...).willReturn(...)` / `then(...).should()`; `@DisplayName` em português; nomenclatura `método_estado_deveComportamento`.

| Framework | Por quê |
|-----------|---------|
| **JUnit 5** | Padrão do ecossistema Spring Boot; `@DisplayName` em português |
| **Mockito (BDD)** | `given/willReturn/then/should` mantém testes legíveis e alinhados à linguagem de negócio |
| **AssertJ** | Assertions encadeadas com mensagens de erro detalhadas |

### Frontend — Vitest + Vue Test Utils

```bash
cd frontend
npm test            # execução única (CI)
npm run test:watch  # modo interativo
npm run test:coverage
```

| Framework | Por quê |
|-----------|---------|
| **Vitest** | Nativo Vite — zero configuração; API idêntica ao Jest |
| **@vue/test-utils** | Monta componentes Vue em jsdom com suporte a stubs, props e emits |

---

## Estrutura do Projeto

```
spassu-livros/
├── microservice/    # Spring MVC — DDD/Hexagonal — negócio + JPA + JasperReports
├── orchestration/   # Spring WebFlux — FlowCockpit — orquestração via RabbitMQ RPC
├── bff/             # Spring WebFlux — gateway — validação JWT + proxy
├── frontend/        # Vue 3 + Vite + Tailwind — SPA com design system próprio
├── infra/           # Keycloak realm, RabbitMQ definitions, Vault scripts, PostgreSQL view
└── docker-compose.yml
```

---

## Desenvolvimento Local

Para rodar os backends no IDE (com hot reload) e apenas a infraestrutura no Docker:

```bash
# 1. Sobe só os middlewares
docker compose -f infra/docker-compose.local.yml up -d

# 2. Inicia cada backend pelas tasks do VS Code, ou manualmente:
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl microservice
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl orchestration
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl bff

# 3. Frontend
cd frontend && npm run dev     # http://localhost:3000
npm run test                   # testes Vitest
```

---

## Contexto do Desafio

> Criar um projeto utilizando as boas práticas de mercado e apresentar o mesmo demonstrando o passo a passo de sua criação (base de dados, tecnologias, aplicação, metodologias, frameworks, etc).

**Instruções originais:**

1. Deve ser feito CRUD para Livro, Autor e Assunto conforme o modelo de dados.
2. A tela inicial pode ter um menu simples ou mesmo links direto para as telas construídas.
3. O modelo do banco deve ser seguido integralmente, salvo para ajustes de melhoria de performance.
4. A interface pode ser simples, mas precisa utilizar algum CSS que comande no mínimo a cor e tamanho dos componentes em tela (utilização do Bootstrap será um diferencial).
5. Os campos que pedem formatação devem possuir o mesmo (data, moeda, etc).
6. Deve ser feito obrigatoriamente um relatório (utilizando o componente de relatórios de sua preferência — Crystal, ReportViewer, etc.) e a consulta desse relatório deve ser proveniente de uma view criada no banco de dados. O relatório deve trazer as informações das 3 tabelas principais agrupando os dados por autor *(atenção: um livro pode ter mais de um autor)*.
7. TDD (Test Driven Development) será considerado um diferencial.
8. Tratamento de erros é essencial; evite try-catch genéricos em situações que permitam tratamentos específicos, como os possíveis erros de banco de dados.
9. O modelo inicial não prevê, mas é necessário incluir um campo de **valor (R$)** para o livro.
10. Guarde todos os scripts e instruções para implantação do projeto — eles devem ser demonstrados na apresentação.

**Modelo de dados original:**

```
Livro           Livro_Autor          Autor
─────────       ───────────          ─────
CodL (PK)  ◄── Livro_CodL (FK)      CodAu (PK)
Titulo          Autor_CodAu (FK) ──► Nome VARCHAR(40)
Editora
Edicao          Livro_Assunto        Assunto
AnoPublicacao   ─────────────        ───────
                Livro_CodL (FK)      codAs (PK)
                Assunto_codAs (FK)──►Descricao VARCHAR(20)
```


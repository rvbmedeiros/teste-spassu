# Spassu Livros

Cadastro de livros desenvolvido como desafio técnico. Implementa CRUD completo de **Livros**, **Autores** e **Assuntos**, geração de relatório PDF, visualização gráfica dos fluxos programáticos Java da orchestration (FlowCockpit) e leitura centralizada de logs, em arquitetura de cinco camadas.

---

## Contexto do Desafio

O projeto consiste no cadastro de livros seguindo modelo de banco pré-definido, com:

- CRUD de Livro, Autor e Assunto (relacionamentos N:N)
- Campo de valor monetário (R$) por livro
- Relatório PDF gerado a partir de uma view SQL, agrupado por autor
- Visualização gráfica dos fluxos da orchestration definidos em Java (`@FlowDefinition`/`@FlowStep`)
- Leitura de logs agregados (BFF, orchestration e microservice) com filtros
- Interface web com CSS, formatação de campos (moeda, etc.) e tratamento de erros

### Rotas principais do frontend

- `/livros` — gestão de livros
- `/autores` — gestão de autores
- `/assuntos` — gestão de assuntos
- `/relatorio` — exportação do relatório PDF
- `/flowcockpit` — visualização gráfica dos fluxos de orquestração
- `/logs` — visualização centralizada de logs da plataforma

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados

### Subindo o ambiente completo

```bash
docker compose up -d --build
```

Aguarde todos os serviços ficarem saudáveis (cerca de 2–3 minutos na primeira execução). A ordem de inicialização é gerenciada automaticamente via `depends_on` + `healthcheck`.

### Acessos

| Serviço | URL | Credencial |
|---------|-----|------------|
| **Frontend** | http://localhost:3000 | `admin / admin123` |
| **BFF (API Gateway)** | http://localhost:8083 | — |
| **Swagger — Orchestration** | http://localhost:8082/swagger-ui.html | — |
| **Logs agregados (API BFF)** | http://localhost:8083/api/logs | JWT (usuário autenticado) |
| **Fluxos (API Orchestration via BFF)** | http://localhost:8083/flows | JWT (usuário autenticado) |
| **AsyncAPI UI — Microservice (Springwolf)** | http://localhost:8081/springwolf/asyncapi-ui.html | — |
| **Keycloak Admin** | http://localhost:8080 | `admin / admin` |
| **RabbitMQ Console** | http://localhost:15672 | `spassu / spassu` |
| **Vault** | http://localhost:8200 | Token: `spassu-dev-token` |
| **Jaeger (traces)** | http://localhost:16686 | — |

> Usuário somente leitura: `usuario / usuario123` (role USER).

---

## Arquitetura

```
Frontend (Vue 3)  :3000
  └─► BFF (WebFlux)  :8083              — gateway; validação JWT; proxy
        └─► Orchestration (WebFlux)  :8082  — orquestração de fluxos (FlowCockpit)
              └─► Microservice (RabbitMQ RPC)  :8081  — regras de negócio (DDD/Hexagonal)
                    └─► PostgreSQL 18  :5432
```

**Suporte:** Keycloak · RabbitMQ · HashiCorp Vault · OpenTelemetry · Jaeger

### Observabilidade e fluxo visual

- **FlowCockpit**: o frontend em `/flowcockpit` consome `GET /flows` para renderizar, em formato gráfico, os fluxos de orquestração cadastrados no módulo Java de orchestration.
- **Logs centralizados**: o frontend em `/logs` consome `GET /api/logs` para exibir logs agregados de BFF, orchestration e microservice.
- **Filtros de logs**: `source`, `level`, `limit` e `search`.
- **Atualização de logs**: ocorre por polling HTTP a cada 5 segundos.

### Flow Cockpit

O FlowCockpit existe para tornar os workflows de negócio legíveis no produto sem exigir leitura de classes Java. A interface mostra eventos, atividades e gateways em formato BPM-like, permitindo que desenvolvimento, QA e negócio entendam o fluxo fim a fim no mesmo ponto de consulta.

**Por que a tecnologia atual foi adotada (annotation scan + grafo estático):**

- Menor atrito com a arquitetura existente (Spring + WebFlux + RabbitMQ RPC), sem introduzir runtime adicional de processo.
- Baixa latência e baixa complexidade operacional para o objetivo atual, que é legibilidade e documentação executável do fluxo.
- Versionamento junto ao código e revisão via PR, mantendo rastreabilidade natural das mudanças de fluxo.

**Por que não Operaton/Camunda 7 como library nesta entrega:**

- A necessidade atual não exige engine BPM stateful (timers, user tasks, histórico de instância, versionamento de execução).
- Incluir engine agora adicionaria sobrecarga de operação e governança sem ganho proporcional para o problema imediato.
- A decisão atual preserva simplicidade; engine BPM pode ser reavaliada quando houver requisitos explícitos de execução de processo além de visualização.

---

## Tecnologias e Justificativas

### Backend

| Tecnologia | Versão | Por quê |
|------------|--------|---------|
| **Java** | 25 | Versão mais recente com ganhos de performance e recursos modernos da linguagem |
| **Spring Boot** | 4.0.4 | Versão mais recente; auto-configuração madura, ecossistema completo |
| **Spring AMQP (RabbitMQ RPC)** | (microservice) | Integração síncrona request-reply e listeners assíncronos sem expor API REST pública |
| **Spring WebFlux** | (orchestration + BFF) | Reativo/não-bloqueante para gateway e orquestração; melhor throughput com I/O intensivo |
| **Spring Data JPA + Hibernate** | — | ORM padrão de mercado; reduz boilerplate de persistência |
| **PostgreSQL** | 18-alpine | Banco relacional robusto; suporte a views e JSON; usado também pelo Keycloak |
| **Spring Cloud Vault** | — | Centraliza secrets fora do código/configuração; todos os módulos consomem via `spring.config.import` |
| **RabbitMQ** | 4-management | Mensageria para RPC síncrono (orchestration → microservice) e eventos assíncronos (created/updated/deleted) |
| **Keycloak** | 26.5.1 | Identity Provider open source; emite JWTs validados pelo BFF via OAuth2 Resource Server |
| **MapStruct** | 1.6.3 | Geração de código de mapeamento em compile-time; zero reflexão em runtime |
| **Lombok** | 1.18.38 | Elimina boilerplate (getters, builders, construtores); mantém foco na lógica |
| **JasperReports** | 7.0.1 | Biblioteca madura de relatórios PDF; alimentada pela view SQL `vw_relatorio_livros` |
| **SpringDoc** | 3.0.0 | Documentação OpenAPI do módulo orchestration |
| **Springwolf** | 2.0.0 | Documentação AsyncAPI para consumers/producers RabbitMQ |
| **OpenTelemetry + Jaeger** | — | Observabilidade distribuída; rastreamento de requisições entre todas as camadas |

### Frontend

| Tecnologia | Versão | Por quê |
|------------|--------|---------|
| **Vue 3** | 3.5.x | Framework progressivo; Composition API + `<script setup>` produz código conciso e testável |
| **Vite** | 7.x | Build tool ultrarrápido; HMR instantâneo em desenvolvimento |
| **TypeScript** | — | Tipagem estática elimina classes inteiras de bug; melhor DX com autocomplete |
| **Tailwind CSS** | 4.x | Utilitário CSS com design tokens via CSS custom properties; dark mode nativo |
| **Pinia** | — | State management oficial Vue; implementa a camada ViewModel do padrão MVVM |
| **vue-i18n** | 10.x | Internacionalização; suporte a `pt-BR` e `en` com Composition API |
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

### Estratégia

Abordagem TDD-first em todas as camadas, com cobertura mínima de 80%. Os testes existem exclusivamente como unidades isoladas — nenhum Spring context é carregado nos testes de backend sem necessidade.

### Backend — JUnit 5 + Mockito

| Tecnologia | Por quê |
|------------|---------|
| **JUnit 5** | Padrão do ecossistema Spring Boot; suporte a `@ExtendWith`, `@DisplayName` em português e nomenclatura `método_estado_deveComportamento` |
| **Mockito (BDD)** | `given(...).willReturn(...)` / `then(...).should()` mantém testes legíveis e alinhados à linguagem de negócio |
| **AssertJ** | Assertions encadeadas e mensagens de erro detalhadas sem verbosidade |

Convenção de uso: `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` — sem `@SpringBootTest`, sem contexto completo. Controllers são testados via instanciação direta com mocks dos use cases.

```bash
# Executa todos os módulos backend
mvn test

# Módulo específico
mvn test -pl microservice
mvn test -pl orchestration
mvn test -pl bff
```

### Frontend — Vitest + Vue Test Utils

| Tecnologia | Por quê |
|------------|---------|
| **Vitest** | Nativo Vite — zero configuração extra; mesma velocidade de HMR nos testes; API idêntica ao Jest |
| **@vue/test-utils** | API oficial para montar componentes Vue em jsdom; suporte a `stubs`, props, emits e slots |
| **jsdom** | Simula DOM no Node sem browser real; suficiente para testes unitários de componentes |

Convenção de uso: `vi.mock()` para isolar dependências (API, Keycloak); `setActivePinia(createPinia())` em `beforeEach` para isolar estado de store; imports relativos nos arquivos de teste (não `@/`). `<Transition>` sempre stubado em testes que verificam remoção de elementos (jsdom não dispara `transitionend`).

```bash
cd frontend
npm test           # execução única (CI)
npm run test:watch # modo interativo
```

---

## Estrutura do Projeto

```
spassu-livros/
├── microservice/    # RabbitMQ RPC — DDD/Hexagonal — negócio + JPA + JasperReports
├── orchestration/   # Spring WebFlux — FlowCockpit — orquestração via RabbitMQ RPC
├── bff/             # Spring WebFlux — gateway — validação JWT + proxy
├── frontend/        # Vue 3 + Vite + Tailwind — SPA com design system próprio
├── infra/           # Keycloak realm, RabbitMQ definitions, Vault scripts, PostgreSQL view
└── docker-compose.yml
```

---

## Desenvolvimento Local

Para rodar os backends no IDE (com hot reload) e somente a infraestrutura no Docker:

```bash
# 1. Sobe só os middlewares
docker compose -f infra/docker-compose.local.yml up -d

# 2. Inicia cada serviço Spring pelas tasks do VS Code
#    ou manualmente (requer VAULT_TOKEN=spassu-dev-token):
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl microservice
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl orchestration
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl bff

# 3. Frontend
cd frontend && npm run dev     # http://localhost:3000
npm run test                   # testes Vitest
```

# Spassu Livros — Copilot Instructions

> Cadastro de livros em cinco camadas. Leia este documento antes de qualquer intervenção no código.

## Arquitetura de Camadas

```
Frontend (Vue 3) :3000
  └─► BFF (WebFlux) :8083          — auth boundary; proxy transparente; normaliza erros
        └─► Orchestration (WebFlux) :8082  — regras de fluxo (FlowCockpit); SEM regras de negócio
              └─► Microservice (MVC) :8081  — regras de negócio (DDD/hexagonal); SEM regras de fluxo
                    └─► PostgreSQL :5432
```

**Serviços de suporte:** Keycloak :8080 | RabbitMQ :5672/:15672 | Vault :8200

### Princípio de responsabilidade por camada

| Camada | Faz | Não faz |
|--------|-----|---------|
| **BFF** | Valida JWT, normaliza ProblemDetail, proxy `/api/**` e `/ws/flows` | Regras de fluxo, regras de negócio |
| **Orchestration** | Orquestra fluxos com `@FlowDefinition/@FlowStep`; FlowCockpit WebSocket | Persistência, validação de negócio |
| **Microservice** | Entidades, UseCases, JPA, JasperReports, RabbitMQ publish | Controle de fluxo, autenticação |
| **Frontend** | MVVM via Pinia; componentes Vue reativos | Lógica de negócio, chamadas diretas ao microservice |

---

## Backend — Convenções Críticas

### Pacotes (`com.spassu.livros.<módulo>`)

**microservice** segue arquitetura hexagonal estrita:
```
domain/model/        ← POJOs puros — zero imports de framework
domain/repository/   ← interfaces Java — sem JPA
domain/event/        ← LivroEvent (record imutável, factory methods)
application/dto/     ← Request/Response DTOs (@Builder + Lombok)
application/mapper/  ← MapStruct interfaces (DTO ↔ Domain)
application/usecase/ ← lógica de aplicação
infrastructure/persistence/entity/   ← @Entity JPA
infrastructure/persistence/mapper/   ← MapStruct (Entity ↔ Domain)
infrastructure/persistence/repository/ ← JpaRepository + Impl
infrastructure/web/  ← Controllers + GlobalExceptionHandler
infrastructure/config/ ← Security (CORS only), RabbitMQ, JasperReports
infrastructure/messaging/ ← LivroEventPublisher
```

**orchestration:**
```
flow/         ← *Flow classes anotadas com @FlowDefinition + @FlowStep
flowcockpit/  ← framework AOP (FlowRegistry, FlowExecutionTracker, FlowWebSocketHandler)
client/       ← MicroserviceClient (RabbitTemplate RPC request-reply)
web/          ← Controllers + FlowController + GlobalExceptionHandler
```

**bff:**
```
web/    ← ProxyController + GlobalExceptionHandler + BffErrorCode (enum)
config/ ← SecurityConfig (@EnableWebFluxSecurity, OAuth2 RS JWT, Keycloak roles)
```

### MapStruct — Regra importante

- Mappers DTO↔Domain são **interfaces** (geração total pelo MapStruct).
- `LivroEntityMapper` é **classe abstrata** porque `Set<LivroAutorEntity>` exige travessia manual de `la.getAutor()`. A classe injeta `AutorEntityMapper` via `@Autowired` e escreve `toDomain()` à mão; `toEntity()` e `updateScalars()` são gerados com `@Mapping(target="autores", ignore=true)`.
- **Nunca converter `LivroEntityMapper` para interface.**

### Segurança

- **Apenas o BFF valida JWT.** Microservice e orchestration não têm Spring Security — confiam na rede interna.
- No BFF: `SecurityConfig` usa `@EnableWebFluxSecurity`, converte `realm_access.roles` → `ROLE_` prefix, CSRF desabilitado.
- Rotas livres no BFF: `/actuator/health`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**`.

### Secrets / Vault

- **Todos** os secrets dos projetos Java vêm do Vault (`spring.config.import: vault://`).
- Paths: `secret/application` (compartilhado) | `secret/spassu/microservice` | `secret/spassu/orchestration` | `secret/spassu/bff`.
- Variáveis de ambiente necessárias em dev: `VAULT_TOKEN=spassu-dev-token` (já configurado nas tasks VS Code).

### RabbitMQ

- Exchange: `livro.events` (TopicExchange durable) com DLX `livro.events.dlx`.
- Filas: `livro.created`, `livro.updated`, `livro.deleted` + DLQ `livro.events.dlq`.
- Publicação é **fire-and-forget**: exceções → `log.warn`, jamais propagadas. Não bloquear o fluxo principal por falha de messaging.
- `LivroEventPublisher` usa `@AsyncPublisher` (Springwolf) para documentação AsyncAPI.

### Comunicação Síncrona (RPC) — Orchestration → Microservice

- Canal: RabbitMQ request-reply via `rabbitTemplate.convertSendAndReceiveAsType()`.
- Exchange RPC: `livros.rpc` (DirectExchange, durable), separado do exchange de eventos.
- Filas de request: `rpc.livros.criar`, `rpc.livros.listar`, `rpc.livros.buscar`, `rpc.livros.atualizar`, `rpc.livros.excluir`, `rpc.autores.criar`, `rpc.autores.listar`, `rpc.autores.buscar`, `rpc.autores.atualizar`, `rpc.autores.excluir`, `rpc.assuntos.criar`, `rpc.assuntos.listar`, `rpc.assuntos.buscar`, `rpc.assuntos.atualizar`, `rpc.assuntos.excluir`, `rpc.relatorio.gerar`.
- Envelope padrão: `RpcResponse<T>(success, payload, errorCode, message, httpStatus)`.
- Orchestration deve encapsular `RabbitTemplate` em `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` para não bloquear o event loop do WebFlux.
- A comunicação entre Orchestration e Microservice é **sempre** RabbitMQ síncrono (request-reply), **nunca REST/WebClient**.
- O header `Authorization` não é repassado no canal interno orchestration→microservice.

### Convenções de código Java

- Injeção via construtor com `@RequiredArgsConstructor` (Lombok) — nunca `@Autowired` em campo.
- DTOs e entities: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`.
- Tabelas de join (`livro_autor`, `livro_assunto`) mapeadas como **entidades explícitas** com `@EmbeddedId` — nunca `@ManyToMany` JPA direto.
- `JOIN FETCH` / `@EntityGraph` **apenas** em carregamento de entidade única. Listas paginadas usam `findAll(pageable)` sem eager fetch.
- `jpa.open-in-view: false` — não alterar.

### Tratamento de erros — GlobalExceptionHandler

Todos os módulos usam `ProblemDetail` (RFC 9457, Spring 6+). Cada módulo tem convenções distintas:

| Módulo | Tipo URI | `timestamp` | Catch-all `Exception` |
|--------|----------|-------------|----------------------|
| **microservice** | Relativo `/errors/not-found` | ✅ (`Instant`) | ❌ intencional |
| **orchestration** | Absoluto `https://spassu.com/problems/…` | ❌ | ✅ |
| **bff** | Absoluto + `BffErrorCode` | ❌ | ✅ |

- Microservice: `EntityNotFoundException` → 404 com `entityName`+`entityId`; `MethodArgumentNotValidException` → **422** com `errors: Map<field,message>`; `DataIntegrityViolationException` → 409. **Nunca capturar `Exception` genérico no microservice.**
- BFF: todo response inclui `code: BffErrorCode` (ex.: `BFF_0002`). Upstream errors incluem `source: "orchestration"` + `orchestrationDetail`.
- `setTitle()` explícito em cada handler do microservice — não confiar no default do Spring.

### Controllers — Convenções

- `@Tag(name=..., description=...)` obrigatório em toda controller.
- `@Operation(summary="...")` obrigatório em todo endpoint. Nunca `@ApiResponse` ou `@Schema` nos DTOs (ainda não adotado).
- Criação: `@ResponseStatus(HttpStatus.CREATED)` no método — **não** retornar `ResponseEntity`.
- Deleção: `@ResponseStatus(HttpStatus.NO_CONTENT)` no método.
- Relatório PDF: exceção — retorna `ResponseEntity<byte[]>` com `Content-Disposition: attachment; filename="relatorio-livros.pdf"`.
- Listagens paginadas: `@PageableDefault(size = 20) Pageable pageable` (microservice MVC) / `@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size` (orchestration WebFlux).
- Orchestration: endpoints não repassam `Authorization` para o microservice; a integração interna é via RPC RabbitMQ.

### UseCases — `@Transactional`

- **`@Transactional` NÃO fica no UseCase** — fica nos métodos do `*RepositoryImpl`:
  - `@Transactional` em `save()`, `deleteById()`
  - `@Transactional(readOnly = true)` em `findById()`, `findAll()`, `existsById()`
- `LivroRepositoryImpl.save()`: chama `saveAndFlush()` para gerar PK antes de sincronizar as tabelas de join.
- `LivroUseCase.resolveAssociations()`: busca autores/assuntos por IDs (`findAllByIds(Set<Integer>)`), limpa as coleções (`livro.getAutores().clear()`) e re-adiciona via domain method (`livro.adicionarAutor(a)`).
- `RelatorioUseCase`: **não usa JPA** — usa `JdbcTemplate` direto na view `vw_relatorio_livros`, alimenta `JRBeanCollectionDataSource`.

### Validação — Restrições por DTO

| Campo | microservice | orchestration |
|-------|-------------|---------------|
| `titulo` | `@NotBlank @Size(max=40)` | idem |
| `editora` | `@Size(max=40)` (opcional) | `@NotBlank @Size(max=40)` ⚠️ diverge |
| `edicao` | `@Positive` | `@NotNull @Min(1)` ⚠️ diverge |
| `anoPublicacao` | `@Pattern(regexp="\\d{4}", message=PT)` | `@Pattern` sem `message` ⚠️ diverge |
| `valor` | `@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2)` | `@NotNull @DecimalMin("0.01")` ⚠️ diverge |
| `autoresCodAu` | `@NotEmpty` | idem |

- `@Valid` sempre no `@RequestBody`, nunca no `@PathVariable`.
- Sem `ConstraintValidator` customizados.
- **Atenção**: microservice é a fonte de verdade para regras de negócio. A validação no orchestration é de defesa rasa — divergências acima devem ser corrigidas se causarem rejeições inesperadas.

### Logging

- **`@Slf4j` (Lombok)** em todos os beans anotados; `LoggerFactory.getLogger(...)` manual apenas em classes não gerenciadas (`FlowWebSocketHandler`).
- **Domínio e UseCases não têm logging** — apenas infrastructure (messaging, config, handlers).
- Fire-and-forget em `LivroEventPublisher`: `log.info(...)` no sucesso, `log.warn(...)` na exceção — nunca relança.
- `FlowWebSocketHandler`: `log.error(...)` com objeto de exceção; `log.trace(...)` para ping/pong WS.
- **Sem MDC**. Sem Log4j — apenas SLF4J.

### Testes (Java)

```
@ExtendWith(MockitoExtension.class)
@Mock / @InjectMocks
given(…).willReturn(…)   // BDD Mockito
then(mock).should().method()
@DisplayName("em português")

// Naming: método_estado_deveComportamento
// Ex: criar_deveResolverAssociacoesEPublicarEvento
//     buscarPorId_quandoNaoEncontrado_deveLancarException
```

---

## Frontend — Convenções Críticas

### MVVM com Pinia

- **Pinia é o único ViewModel.** Componentes Vue não fazem chamadas HTTP diretamente — toda lógica de dados passa pela store.
- Stores de domínio (`livros`, `autores`, `assuntos`) seguem o padrão:

```ts
export const useFooStore = defineStore('foos', () => {
  const items = ref<Foo[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true; error.value = null
    try { items.value = (await api.get<Foo[]>('/api/foos')).data }
    catch (e: unknown) { error.value = extractApiErrorMessage(e) }
    finally { loading.value = false }
  }
  // criar → POST → fetchAll()
  // atualizar → PUT → fetchAll()
  // excluir → DELETE → filtro local otimista (sem refetch)

  return { items, loading, error, fetchAll, criar, atualizar, excluir }
})
```

- `flows.ts` é exceção: WebSocket com auto-reconexão (`setTimeout(connectWs, 3000)`) e buffer máximo de 200 eventos.

### Padrão de formulários nas Views

```vue
<form @submit.prevent="save">
  <BaseInput :label="t('livros.titulo')" v-model="form.titulo" required>
    <template #leading><BookOpen :size="16" /></template>
  </BaseInput>
  <!-- campos numéricos: v-model.number -->
</form>
```

- `showModal = ref(false)`. Abertura via `openCreate()` (limpa form) ou `openEdit(item)` (preenche form + define `editingId`).
- Confirmação de exclusão: `confirmDeleteId = ref<number | null>(null)` — UI inline condicional, **não** usa modal separado.
- Toast local: `toast = ref<{message: string, type: 'success'|'error'} | null>(null)` → `<BaseToast>` com auto-dismiss.
- Exibição de erro da store: `v-else-if="store.error"` com classe `text-red-500` — sem componente dedicado.
- Formatação de moeda **inline no template** com `Intl.NumberFormat`:
  ```js
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(livro.valor)
  ```

### TypeScript — Interfaces e Tipos

- Interfaces colocadas **no mesmo arquivo da store** que as define — sem `types.ts` global.
- Exports nomeados; import no consumidor: `import type { Autor } from './autores'`.
- Interfaces exportadas: `Autor` (autores.ts), `Assunto` (assuntos.ts), `Livro`/`LivroRequest` (livros.ts), `StepExecutedEvent`/`FlowNode`/`FlowGraph` (flows.ts).
- `e: unknown` em todos os `catch` — nunca `e: any`.

### Naming de componentes

| Padrão | Uso |
|--------|-----|
| `Base*` | Primitivos reutilizáveis em `components/base/` (Button, Card, Input, Modal, Switcher, Toast) |
| `The*` | Singleton por página (ex.: `TheNavbar`) |
| `*View.vue` | Telas em `views/` |

- Sempre `<script setup lang="ts">` (Composition API). **Nunca Options API.**
- Props tipadas com `defineProps<{...}>()` + `withDefaults`. Emits com `defineEmits<{...}>()`.

### Tailwind v4 + Design Tokens

- Tailwind v4 via plugin `@tailwindcss/vite` — **sem** `tailwind.config.js`.
- Tokens em `src/assets/main.css` via CSS custom properties: `--ui-bg`, `--ui-brand`, `--ui-text`, `--ui-border`, `--ui-danger`.
- Dark mode: bloco `.dark {}` no CSS togglado em `<html>` pelo `useUiStore.toggleTheme()`.
- Referenciar tokens como `text-(--ui-brand)` (sintaxe Tailwind v4) ou `text-[color:var(--ui-brand)]`.
- Ícones exclusivamente de **`lucide-vue-next`** — nunca outra biblioteca de ícones.

### i18n

- `vue-i18n` v10, modo Composition API (`legacy: false`), locale padrão `pt-BR`, fallback `en`.
- Locale persistido em `localStorage('locale')`.
- Keys namespaceadas: `livros.*`, `autores.*`, `assuntos.*`, `nav.*`, `common.*`.
- **Todo texto visível ao usuário deve ter chave i18n** — nunca string literal no template.

### API / Auth

- Axios em `services/api.ts`; `baseURL: ''` em dev → proxy Vite repassa para BFF `:8083`.
- Interceptor de request: `keycloak.updateToken(30)` + `Authorization: Bearer`.
- Interceptor de resposta: 401/403 → `keycloak.login()`.
- Erros: `extractApiErrorMessage(e)` de `services/errors.ts` — mapeia `BffErrorCode` → chave i18n.

### Testes (Frontend)

```bash
npm run test          # vitest run (CI)
npm run test:watch    # interativo
npm run test:coverage # v8 coverage
```

- Setup em `src/tests/setup.ts` — monta `vue-i18n` globalmente via `config.global.plugins`.
- Stores: `vi.mock('@/services/api', ...)` + `setActivePinia(createPinia())` em `beforeEach`.
- Componentes: `mount()` do `@vue/test-utils` com `global.plugins: [i18n]`.

### CORS

- Origem permitida vem do Vault: `${spassu.allowed-origins}` (string separada por vírgula).
- Cada módulo usa a API Spring adequada: `WebMvcConfigurer` (microservice MVC), `CorsWebFilter @Bean` (orchestration), `CorsConfigurationSource` embutido no `SecurityFilterChain` (BFF).
- Mesma política em todos: métodos `GET/POST/PUT/DELETE/OPTIONS`, headers `*`, `allowCredentials(true)`, mapeado em `/**`.

### Paginação — Limitação Arquitetural Conhecida

- **Microservice** retorna `Page<T>` completo (totalElements, totalPages, content).
- **Orchestration** consome via RPC (`RpcResponse<List<T>>`) no `MicroserviceClient`, sem usar `bodyToFlux`.
- **Frontend** busca sempre `page=0&size=20` e exibe todos os itens sem controles de navegação.
- Qualquer implementação de paginação na UI exige propagar metadados de paginação no contrato RPC (ex.: `PagedResult<T>` em `payload`).

---

## Comandos de Build e Run

```bash
# Build completo (todos os módulos)
mvn clean verify

# Build sem testes
mvn clean install -DskipTests

# Dev local (backends) — via tasks do VS Code ou:
VAULT_TOKEN=spassu-dev-token mvn spring-boot:run -pl microservice \
  -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5801"

# Portas de debug: microservice=5801, orchestration=5802, bff=5803

# Frontend
cd frontend && npm run dev    # Vite :3000
npm run test                  # testes

# Infra completa (Docker)
docker compose --env-file .env.docker up -d --build

# Parcial (só infra, backends no IDE)
docker compose --env-file .env.docker up -d postgres keycloak rabbitmq vault vault-init
```

---

## Acessos Locais

| Serviço | URL | Credencial |
|---------|-----|------------|
| Frontend | http://localhost:3000 | admin/admin123 |
| Swagger microservice | http://localhost:8081/swagger-ui.html | — |
| Swagger orchestration | http://localhost:8082/swagger-ui.html | — |
| Keycloak Admin | http://localhost:8080 | admin/admin@2026! |
| RabbitMQ | http://localhost:15672 | spassu/spassu@2026! |
| Vault | http://localhost:8200 | Token: spassu-root-token-dev |

Usuários de teste: `admin/admin123` (ADMIN) | `usuario/usuario123` (USER, somente leitura).

---

## Armadilhas e Decisões Importantes

- **Padrão obrigatório de entrega do desafio:** o avaliador deve executar apenas `docker compose up -d --build` em `docker-compose.yml` e ter o ambiente funcional sem qualquer intervenção manual.
- **`vw_relatorio_livros` deve existir automaticamente no bootstrap Docker.** Como `ddl-auto: update` não cobre VIEW, a criação da view deve ser automatizada via container de inicialização SQL do PostgreSQL (nunca manual).
- **`LivroEntityMapper` é abstract class** — não converter para interface (veja seção MapStruct acima).
- **`flyway.enabled: false` + `ddl-auto: update`** — conveniência de dev; não habilitar Flyway sem migrar os scripts em `db/migration`.
- **Não adicionar validação de JWT no microservice ou orchestration** — a arquitetura confia na rede interna deliberadamente.
- **Não usar `@ManyToMany`** para `livro_autor` ou `livro_assunto` — as entidades de join são explícitas para controle completo.
- **Não usar `JOIN FETCH` em listagens paginadas** — apenas em carregamento de entidade única (`findWithAssociationsByCodL`).
- **`jpa.open-in-view: false`** — não reverter; todo dado necessário deve ser carregado dentro da transação do UseCase.
- **Pinia é o único ViewModel** — nunca buscar dados via `fetch`/`axios` diretamente em componentes Vue.
- **Todo secret Java vai para o Vault** — nunca colocar credencial em `application.yml`.
- **Não rode comandos mvn e npm sem pedir permissão antes ao usuário**
- **Em toda sessão de Copilot Chat que implemente qualquer funcionalidade, correção ou refatoração, é obrigatório implementar também os testes UT/SIT correspondentes, sem exceção.**

---

## Referências Internas

- Proposta original: [proposta.md](../proposta.md)
- Documento do desafio: [docs/desafio.txt](../docs/desafio.txt)
- Realm Keycloak: [infra/keycloak/realm-export.json](../infra/keycloak/realm-export.json)
- Seed de secrets Vault: [infra/vault/vault-init.sh](../infra/vault/vault-init.sh)
- Design tokens CSS: [frontend/src/assets/main.css](../frontend/src/assets/main.css)

---

## Sugestões de Padrões de Mercado (não adotados)

Itens identificados como ausentes em relação a boas práticas do ecossistema. Cada um é independente e pode ser adotado incrementalmente.

### 🔴 Alta prioridade (impacto direto na qualidade)

**1. Composable `useFormatter` no frontend**
Formatação de moeda está inline em 1 lugar hoje mas tende a proliferar. Extrair para composable evita divergência:
```ts
// src/composables/useFormatter.ts
export function useFormatter() {
  const currency = (v: number) =>
    new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v)
  return { currency }
}
```

**2. Alinhar validações entre orchestration e microservice**
`editora` é opcional no microservice mas obrigatória no orchestration — causará 422 do orchestration para dados válidos no microservice. Regra: orchestration deve ter validação igual ou mais fraca que o microservice. Ver tabela de validação acima.

**3. Corrigir passagem de metadados de paginação**
Mesmo com RPC (`RpcResponse<List<T>>`), os metadados de paginação ainda não chegam ao frontend. Introduzir um wrapper de payload paginado (ex.: `PagedResult<T>`) e propagar `totalElements`/`totalPages` até a UI. Ver seção "Paginação" acima.

**4. Flyway migrations para produção**
`ddl-auto: update` + `flyway.enabled: false` é inaceitável em produção. Scripts SQL devem existir em `microservice/src/main/resources/db/migration/V*.sql`. A ativação do Flyway é a única mudança necessária; o banco atual pode ser base para `V1__init.sql` via `pg_dump --schema-only`.

### 🟡 Média prioridade (observabilidade e operação)

**5. Trace ID com MDC entre camadas**
Nenhuma camada propaga trace context, dificultando correlação de logs em produção. Abordagem mínima:
- BFF: gerar `X-Request-ID` (UUID) se ausente, adicionar `MDC.put("traceId", ...)` em filtro WebFlux.
- Propagar cabeçalho `X-Request-ID` em todas as chamadas WebClient downstream.
- Orchestration e Microservice: ler o header e adicionar ao MDC em `WebMvcConfigurer.addInterceptors`.
- Alternativa completa: Micrometer Tracing + OpenTelemetry (já no ecossistema Spring Boot 4).

**6. CI/CD mínimo com GitHub Actions**
O projeto não tem nenhuma automação. Um workflow básico que já traria valor:
```yaml
# .github/workflows/ci.yml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '25', distribution: 'temurin' }
      - run: mvn verify -DskipTests
  test:
    runs-on: ubuntu-latest
    steps:
      - run: mvn test
      - run: cd frontend && npm ci && npm test
```

**7. Security headers no nginx / BFF**
Nenhuma camada seta headers de segurança HTTP. Adicionar no nginx (`infra/nginx/default.conf`) ou via `WebFilter` no BFF:
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Content-Security-Policy: default-src 'self'`

### 🟢 Baixa prioridade (developer experience)

**8. OpenAPI `@ApiResponse` padronizado**
Hoje nenhum endpoint documenta respostas de erro no Swagger. Adicionar a nível de classe ou via `@OpenAPIDefinition` global os responses 400, 404, 422, 500 melhora a contratos de API.

**9. Composable `useFormErrors` no frontend**
Field-level validation hoje não existe no frontend — o usuário só vê o erro da store (string genérica). Um composable que consome `errors: Map<field, message>` do ProblemDetail 422 melhoraria a UX:
```ts
// src/composables/useFormErrors.ts
export function useFormErrors() {
  const fieldErrors = ref<Record<string, string>>({})
  function parseApiError(e: unknown) { /* extrai errors do ProblemDetail 422 */ }
  function clearErrors() { fieldErrors.value = {} }
  return { fieldErrors, parseApiError, clearErrors }
}
```

**10. `.editorconfig` para consistência entre editores**
Sem `.editorconfig`, tabs vs spaces e fim de linha variam por editor. Mínimo recomendado:
```ini
root = true
[*]            indent_style = space; end_of_line = lf; charset = utf-8; indent_size = 4
[*.{vue,ts,js,json,yml,yaml}]   indent_size = 2
```

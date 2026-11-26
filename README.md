# OnlyStudent Backend

API REST para uma plataforma educacional. O backend permite que estudantes e
educadores gerenciem contas, perfis, turmas, matrículas, materiais e avisos;
também oferece recursos administrativos e envio de mensagens ao suporte.

## Tecnologias

- Java 25 e Spring Boot 3.5.16
- Spring MVC, Spring Security, Bean Validation e Spring Data JPA/Hibernate
- PostgreSQL 16
- Maven Wrapper (Maven 3.9.7)
- JWT (Auth0 java-jwt) e documentação OpenAPI (springdoc)
- JUnit, Spring Boot Test, Spring Security Test e REST Docs para testes
- Docker e Docker Compose para build e execução da API e do PostgreSQL

O schema é atualizado pelo Hibernate (`ddl-auto=update`; nos testes, `create-drop`).
Não há ferramenta de migração de schema configurada. O projeto não declara
cache ou mensageria. O envio de suporte integra com SMTP via Spring Mail.

## Organização

```text
src/main/java/com/sloth/OnlyStudent/
├── application/    # Casos de uso e mapeadores por funcionalidade
├── controller/     # Controllers REST
├── entities/       # Entidades JPA, domínio e DTOs
├── infra/          # Configuração OpenAPI e segurança
├── repository/     # Repositórios Spring Data JPA
├── services/       # Serviços existentes e agendamento de exclusões
├── shared/errors/  # Exceções e tratamento global de erros
└── OnlyStudentApplication.java
```

Os casos de uso em `application/` concentram a lógica da aplicação. Os
controllers fazem o binding e a validação das requisições e delegam as
operações aos casos de uso ou, em alguns fluxos legados, a `services/`.
As entidades e os DTOs ainda compartilham o pacote `entities/DTO`.

O ponto de entrada é `OnlyStudentApplication`. Há também uma tarefa agendada
que executa diariamente à meia-noite e remove turmas cuja exclusão foi
programada.

Documentação complementar:

- [Arquitetura](docs/ARQUITETURA.md)
- [Decisões arquiteturais](docs/DECISOES-ARQUITETURAIS.md)
- [Changelog de contrato da API](docs/CHANGELOG-CONTRATO.md)

## Pré-requisitos

- JDK 25
- Docker com Docker Compose
- Bash para os scripts `.sh`
- OpenSSL para gerar uma chave JWT local com o comando abaixo
- `jq` somente para `scripts/seed-demo.sh`

Não é necessário instalar Maven separadamente: use `./mvnw`.

## Configuração

Na configuração padrão (`application.properties`), a aplicação exige:

| Variável | Uso |
| --- | --- |
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave usada para assinar tokens JWT |
| `PORT` | Porta HTTP; padrão `8082` |
| `CORS_ALLOWED_ORIGINS` | Origens CORS separadas por vírgula; padrão `http://localhost:5173,http://localhost:3000` |

No Compose, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`,
`DB_PORT` e `APP_PORT` podem ser definidos pelo ambiente ou por um arquivo
`.env` local (ignorado pelo Git). Os defaults do Compose são apenas para
desenvolvimento local e não devem ser usados em produção. `SPRING_MAIL_HOST`,
`SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME` e `SPRING_MAIL_PASSWORD` são
opcionais; quando fornecidos, são encaminhados ao serviço da API.

Os limites de requisição podem ser ajustados pelas propriedades
`app.auth.rate-limit.requests`, `app.auth.rate-limit.window-seconds`,
`app.support.rate-limit.requests` e `app.support.rate-limit.window-seconds`.
Os padrões são 10 requisições de autenticação por 60 segundos e 5 requisições
de suporte por 3600 segundos; os contadores são mantidos em memória por
instância.

Use valores locais próprios para credenciais e uma chave JWT forte; não
publique segredos nem os inclua em commits.

### Perfis disponíveis

- **Padrão:** usa `application.properties`; banco e JWT vêm das variáveis
  listadas acima.
- **`dev`:** `application-dev.properties` aponta por padrão para PostgreSQL
  em `localhost:5432`, banco e usuário `onlystudent`, com senha
  `onlystudent-local-dev`; ativa logs SQL. `DB_URL`, `DB_USERNAME`,
  `DB_PASSWORD` e `JWT_SECRET` podem sobrescrever esses valores.
- **`homolog`:** `application-homolog.properties` aceita `DB_URL`,
  `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`. Também permite `PGHOST`,
  `PGPORT` (padrão `5432`), `PGDATABASE`, `PGUSER` e `PGPASSWORD` como
  alternativas para as credenciais do banco; a porta HTTP usa `PORT`
  (padrão `8080`). A exposição do Actuator nesse perfil inclui o health check.
- **`test`:** configuração em `src/test/resources/application-test.properties`,
  usada pelos testes; o banco padrão esperado é `onlystudent-test`, em
  `localhost:5432`.

Não há arquivo `application-prod.properties` no repositório. O arquivo
`application-dev.properties.example` e
`application-local.properties.example` são referências para configuração,
não substituem as configurações ativas.

O endpoint de suporte depende de um `JavaMailSender` configurado no ambiente.
O código não fornece servidor SMTP nem credenciais; sem essa integração,
`POST /support/send` retorna erro de serviço não configurado. O endereço
remetente/destinatário usa `spring.mail.username` e, se ausente, o fallback
`no-reply@onlystudent.local`.

## Executar com Docker Compose

O Compose constrói a imagem e sobe o PostgreSQL e a API. O banco fica
disponível apenas em `localhost:5432`; a API, em `http://localhost:8082`.
Os valores de desenvolvimento permitem iniciar sem configurar um `.env`:

```bash
docker compose up -d --build
```

O serviço `app` aguarda o healthcheck do PostgreSQL e possui seu próprio
healthcheck em `/actuator/health`. Para acompanhar a inicialização e os logs:

```bash
docker compose logs -f app db
```

Para mudar credenciais ou a chave JWT, defina `POSTGRES_PASSWORD` e
`JWT_SECRET` no ambiente ou em `.env` antes de iniciar. O valor JWT padrão é
apenas para desenvolvimento local. O suporte por e-mail só funciona quando
as variáveis SMTP opcionais estão configuradas.

Para parar os serviços sem remover os dados persistidos:

```bash
docker compose down
```

O script `./run-dev.sh` continua disponível para iniciar o banco via Compose
e executar a aplicação no host, em vez de iniciar o serviço `app`.

Para compilar e empacotar sem executar os testes:

```bash
./mvnw clean package -DskipTests
```

Para construir a imagem Docker:

```bash
docker build -t onlystudent-backend .
```

O Dockerfile e a configuração da aplicação usam a porta `8082`.

## Testes

Os testes de integração usam o perfil `test` e precisam de PostgreSQL. O
Compose cria o banco `onlystudent-test` na inicialização do volume. Inicie o
banco e rode a suíte com os defaults alinhados:

```bash
docker compose up -d db
./mvnw clean test
```

O perfil de teste usa o banco `onlystudent-test`, usuário `onlystudent` e a
senha local padrão do Compose. O workflow de CI define esses valores por
`TEST_DB_URL`, `TEST_DB_USERNAME` e `TEST_DB_PASSWORD`.

O workflow de CI em `.github/workflows/backend-ci.yml` executa
`./mvnw clean test` com JDK 25 e PostgreSQL 16.

## API e autenticação

O Swagger UI está disponível em `/swagger-ui.html` e o documento OpenAPI em
`/v3/api-docs`. Ambos são públicos. Para rotas protegidas, use
`Authorization: Bearer <JWT>`; as permissões dependem dos papéis `ADMIN`,
`EDUCATOR` e `STUDENT`. As principais áreas da API são:

| Área | Rotas representativas | Acesso |
| --- | --- | --- |
| Autenticação | `POST /auth/register`, `POST /auth/login`; `GET /auth/educator`, `/auth/student` e `/auth/common` | Registro e login públicos; recursos protegidos pelo papel |
| Perfis | `/student/profile`, `/student/atualUser`, `/educator/profile`, `/educator/atualUser` (`GET`/`PUT`) | Estudante ou educador autenticado |
| Turmas | `/turma/register`, `/turma/search`, `/turma/allClassroom`, `/turma/{codigo}`, `/turma/{codigo}/entrar` | Leitura e alterações protegidas conforme papel e vínculo com a turma |
| Materiais | `POST /material/register`, `PUT /material/{id}`, `DELETE /material/delete/{id}`, `GET /turma/{codigo}/materials` | Operações de material exigem educador; leitura de materiais da turma exige autenticação |
| Avisos | `/turma/{codigo}/avisos` e `/turma/{turmaId}/avisos/{avisoId}` (`GET`/`POST`/`PUT`/`DELETE`) | Leitura conforme regras da rota; alterações exigem autenticação e autorização |
| Administração | `/admin`, `/admin/{id}`, `/admin/admin-resource` | Papel `ADMIN` |
| Suporte e token | `POST /support/send`, `GET /token/validate` | Suporte público com limite padrão de 5 requisições por hora por instância; validação recebe o token no header `Authorization` |
| Saúde | `GET /actuator/health` | Público |

Os caminhos e permissões acima resumem as rotas implementadas; consulte a
especificação OpenAPI para parâmetros, payloads e respostas. Erros da API são
tratados globalmente e várias respostas de erro são texto puro.

## Scripts auxiliares

- `scripts/create-test-db.sql`: cria o banco `onlystudent-test` na inicialização
  do PostgreSQL.
- `scripts/seed-demo.sh`: cria usuários e turmas de demonstração usando a API
  local; requer `jq` e deve ser usado somente em ambiente local.
- `run-dev.sh`: sobe o PostgreSQL e inicia a aplicação no host, usando o perfil
  `dev` e uma porta local alternativa para o banco.

## Pendências / Observações

- A configuração CORS lista `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS`, mas
  não `PATCH`, embora existam rotas `PATCH` de turma; clientes web podem
  falhar no preflight dessas rotas.
- O SMTP necessário para o endpoint de suporte deve ser fornecido pelo
  ambiente de execução; não há configuração de servidor ou credenciais no
  repositório.
- Volumes PostgreSQL existentes não são reinicializados com as novas
  variáveis nem executam novamente `scripts/create-test-db.sql`. Faça backup e
  migre os dados/credenciais manualmente antes de reutilizar um volume antigo.

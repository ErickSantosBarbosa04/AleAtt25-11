# ThinDesk — Controle de chamados técnicos

Sistema de atendimento técnico com cadastro público, login individual, dashboard e acompanhamento de chamados. Desenvolvido com Java 21, Spring Boot, Spring Security, Thymeleaf e MongoDB.

## Aplicação publicada

**[Acessar o ThinDesk](https://thindesk-production-732de546.up.railway.app/login)** · **[Criar minha conta](https://thindesk-production-732de546.up.railway.app/cadastro)**

Qualquer pessoa pode criar uma conta comum, sem aprovação manual e sem conta no Railway ou GitHub:

1. Abra o link e clique em **Criar minha conta**.
2. Escolha um usuário de 3 a 30 caracteres, usando letras, números, ponto, traço ou sublinhado.
3. Defina uma senha de 8 a 64 caracteres e confirme-a.
4. Depois da confirmação do cadastro, entre com o usuário e a senha que você escolheu.

Cada cadastro público recebe exclusivamente o perfil **USER (usuário comum)**. A pessoa pode abrir chamados e acompanhar apenas os seus próprios registros. O dashboard também é individual. A resolução de chamados e a consulta de clientes são funções administrativas.

As contas são persistidas no MongoDB. As senhas são armazenadas como hashes BCrypt; senhas e confirmação não são exibidas após o cadastro. Nomes de usuário são normalizados para minúsculas e usados como identificador único do MongoDB. A gravação por inserção impede que um cadastro substitua uma conta existente. O cadastro não aceita a escolha de perfil administrativo.

Este é um projeto acadêmico: utilize apenas dados fictícios nos chamados.

## Versionamento semântico

**Versão atual: `1.2.0-beta.1`**, declarada no `pom.xml` e na interface.

Formato adotado: **MAJOR.MINOR.PATCH[-PRÉ-LANÇAMENTO]**, conforme o [Semantic Versioning 2.0.0](https://semver.org/lang/pt-BR/).

| Parte | Valor | Regra |
| --- | --- | --- |
| MAJOR | `1` | Linha principal; mudanças incompatíveis de uma versão estável avançam esse número. |
| MINOR | `2` | Nova funcionalidade: cadastro público com contas individuais e controle de acesso por perfil. |
| PATCH | `0` | Primeira entrega desta linha; correções posteriores sem novas funcionalidades incrementam este número. |
| Pré-lançamento | `beta.1` | Primeira beta da linha `1.2`, disponível para avaliação e sujeita a ajustes. |

O código recebido tinha a identificação `1.0-alpha`, sem os três números exigidos pelo SemVer. A linha `1.1.0-beta.1` adicionou o dashboard e o deploy. A linha `1.2.0-beta.1` substitui o acesso compartilhado por cadastro individual. Como são versões beta, o contrato de autenticação ainda está em evolução. Exemplo de sequência: `1.2.0-beta.1` → `1.2.0-beta.2` → `1.2.0` → `1.2.1` → `1.3.0`.

## Dashboard

![Dashboard real do ThinDesk](docs/dashboard.jpg)

A captura corresponde ao código deste repositório. O usuário comum visualiza o total, os chamados abertos e resolvidos, a taxa de resolução e os cinco chamados mais recentes **da sua própria conta**. Administradores visualizam o conjunto de chamados. Os valores vêm do MongoDB e variam conforme o uso.

Os exemplos fictícios da inicialização são visíveis somente para administradores. O cadastro de um novo usuário começa sem chamados.

## Permissões

| Ação | Usuário comum | Administrador |
| --- | --- | --- |
| Criar a própria conta pelo cadastro público | Sim | O cadastro público sempre cria usuário comum. |
| Abrir chamados | Sim | Sim |
| Ver dashboard, lista e detalhes dos chamados | Apenas os próprios | Todos |
| Resolver chamados | Não | Sim |
| Consultar ou cadastrar clientes | Não | Sim |
| Acessar ajustes de horários | Não | Sim |

A autorização é validada no servidor, inclusive ao acessar diretamente a API ou informar o ID de um chamado de outra pessoa. Os formulários, o login e as operações da API utilizam proteção CSRF.

## Executar localmente

Pré-requisitos: JDK 21 e MongoDB acessível na porta `27017`.

```powershell
.\mvnw.cmd spring-boot:run
```

Abra `http://localhost:8080/cadastro`, crie sua conta e entre pelo login.

Para usar outra conexão MongoDB:

```powershell
$env:MONGODB_URI = 'mongodb://localhost:27017/thindesk'
.\mvnw.cmd spring-boot:run
```

Com Maven instalado, para compilar, testar e gerar o executável:

```text
mvn clean verify
java -jar target/thindesk.jar
```

Os testes precisam de MongoDB disponível. O workflow `.github/workflows/verify.yml` usa um MongoDB isolado no GitHub Actions.

## Deploy e configuração

O Railway acompanha a branch `main` deste repositório. O `Dockerfile` compila o projeto e executa o JAR com Java 21; o `railway.toml` define a verificação de disponibilidade em `/login`. O serviço MongoDB usa rede privada e volume persistente.

| Variável | Finalidade |
| --- | --- |
| `MONGODB_URI` | Conexão privada com MongoDB; no Railway pode usar `${{MongoDB.MONGO_URL}}`. |
| `PORT` | Porta atribuída pela hospedagem; localmente usa `8080`. |
| `ADMIN_USERNAME` | Opcional: nome de uma conta administrativa provisionada pelo responsável pelo deploy. |
| `ADMIN_PASSWORD` | Opcional: segredo da conta administrativa, definido na hospedagem. Não há valor padrão. |

A conta administrativa só é criada quando as duas variáveis opcionais estão configuradas e o nome ainda não existe. Uma conta comum existente nunca é promovida por essa rotina. A senha administrativa deve ter pelo menos 12 caracteres e no máximo 72 bytes. O cadastro público não depende dessas variáveis.


A disponibilidade depende da manutenção dos serviços e dos limites/créditos da conta Railway.

## API REST

As rotas exigem sessão autenticada. Para operações de escrita, envie também o token CSRF. O solicitante e o técnico são obtidos da sessão, sem aceitar esses papéis no corpo da requisição.

| Método | Rota | Acesso |
| --- | --- | --- |
| `POST` | `/api/chamados` | Usuário autenticado; campos `titulo` e `descricao`. |
| `GET` | `/api/chamados/usuario/{id}` | Próprio nome de usuário ou administrador. |
| `GET` | `/api/chamados/{id}` | Proprietário do chamado ou administrador. |
| `PUT` | `/api/chamados/{id}/resolver` | Administrador; campo `solucao`. |

Respostas previstas: `201` ao criar, `400` para dados inválidos, `401` para API sem autenticação, `403` para falta de permissão ou token CSRF inválido, `404` para chamado inexistente e `409` para resolução duplicada.

A coleção `postman/ThinDesk-Chamados.postman_collection.json` exige que você preencha suas próprias credenciais. As operações de resolução dependem de uma conta administrativa.

## Validação e entrega

A rotina de integração cobre cadastro público, validações, nome duplicado, armazenamento de senha com hash, login e logout, perfil comum obrigatório, proteção CSRF, isolamento dos chamados e resolução administrativa. Os resultados ficam em [GitHub Actions](https://github.com/ErickSantosBarbosa04/AleAtt23-10/actions).

| Requisito da atividade | Entrega |
| --- | --- |
| Versionamento semântico (1 ponto) | Versão no `pom.xml` e explicação neste README. |
| Print de dashboard compatível com o ZIP (1 ponto) | `docs/dashboard.jpg` e tela implementada neste repositório. |
| Deploy funcionando com login para qualquer pessoa (3 pontos) | Links de acesso e cadastro público no início deste README. |

Baixe o ZIP atualizado da branch `main` para entregar o mesmo código do deploy e da documentação.

# ThinDesk

Projeto de atendimento t�cnico com uma API REST para abertura, consulta e resolu��o de chamados. A API de chamados est� integrada � aplica��o Spring Boot do ThinDesk.

## Tecnologias

- Java 23
- Spring Boot 3.4.0
- Spring Web e Spring Security
- Spring Data MongoDB
- MongoDB
- Thymeleaf para as p�ginas existentes do sistema
- Maven Wrapper

## Executar localmente

Pr�-requisitos: JDK 23 e uma inst�ncia MongoDB acess�vel.

1. Inicie o MongoDB local na porta padr�o `27017`.
2. No terminal, na raiz do projeto, execute:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Por padr�o, a aplica��o usa `mongodb://localhost:27017/thindesk`. Para configurar outra URI, defina a vari�vel de ambiente `MONGODB_URI` antes de iniciar a aplica��o.

A aplica��o fica dispon�vel em `http://localhost:8080`. A tela de login fica em `http://localhost:8080/login`.

### Acesso de demonstra��o

O `application.properties` define o usu�rio de demonstra��o `admin` e a senha `thindesk`. Troque essas credenciais antes de usar o projeto em um ambiente compartilhado ou de produ��o.

## API de chamados

As rotas da API exigem autentica��o pela sess�o do Spring Security. Ao abrir ou resolver um chamado, o sistema usa o usu�rio autenticado para preencher o solicitante ou o t�cnico.

| M�todo | Rota | Descri��o |
| --- | --- | --- |
| `POST` | `/api/chamados` | Abre um chamado |
| `GET` | `/api/chamados/usuario/{id}` | Lista chamados do usu�rio |
| `GET` | `/api/chamados/{id}` | Consulta um chamado pelo ID |
| `PUT` | `/api/chamados/{id}/resolver` | Registra a solu��o e resolve um chamado |

### Abrir chamado

`POST /api/chamados`

```json
{
  "titulo": "Computador sem acesso � rede",
  "descricao": "A esta��o n�o consegue acessar a rede."
}
```

O servidor registra o solicitante autenticado, a data de abertura e o status inicial `ABERTO`. Em caso de sucesso, retorna `201 Created`.

### Listar chamados do usu�rio

`GET /api/chamados/usuario/admin`

No c�digo atual, o valor `{id}` dessa rota corresponde ao nome do usu�rio salvo no chamado (por exemplo, `admin`).

### Consultar chamado

`GET /api/chamados/{id}`

Retorna os dados do chamado, incluindo status, solicitante, datas, solu��o e t�cnico, quando informados.

### Resolver chamado

`PUT /api/chamados/{id}/resolver`

```json
{
  "solucao": "Reiniciada a interface de rede e validado o acesso."
}
```

Somente chamados `ABERTO` podem ser resolvidos. A resolu��o registra o usu�rio autenticado como t�cnico, a solu��o, a data e o status `RESOLVIDO`. Uma nova tentativa de resolver o mesmo chamado retorna `409 Conflict`.

### Valida��es e respostas comuns

- Campos obrigat�rios vazios: `400 Bad Request`.
- Requisi��o sem autentica��o: `401 Unauthorized` nas rotas da API.
- Chamado inexistente: `404 Not Found`.
- Tentativa de resolver chamado que n�o est� aberto: `409 Conflict`.

Os chamados s�o persistidos na cole��o `chamados` do banco `thindesk`.

## Testar com Postman

1. Inicie o MongoDB e a aplica��o.
2. No Postman Desktop, importe `postman/ThinDesk-Chamados.postman_collection.json`.
3. Execute a cole��o na ordem apresentada. A primeira requisi��o autentica e as seguintes usam a sess�o.

A cole��o percorre o login, a abertura, as consultas, a resolu��o, uma consulta posterior para confirmar os dados persistidos e uma tentativa de resolu��o duplicada. O `409 Conflict` da �ltima requisi��o � esperado e confirma a regra de bloqueio.

<h1 align="center">Educa+</h1>

<p align="center">
  Plataforma educacional gamificada com tutor de IA, painel para professores e gestão para a coordenação.<br>
  Banco MySQL, API Spring Boot e app Android.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white">
  <img alt="Android" src="https://img.shields.io/badge/Android-minSdk%2026-3DDC84?logo=android&logoColor=white">
</p>

<!-- Dica: coloque 2 ou 3 prints do app em docs/img/ e mostre aqui:
<p align="center">
  <img src="docs/img/mapa.png" width="220">
  <img src="docs/img/insights.png" width="220">
</p>
-->

## Sobre o projeto

O Educa+ é um aplicativo Android em que o aluno aprende jogando: cada disciplina é uma ilha, cada ilha tem uma trilha de níveis e um tutor (o **Tico**) acompanha o aluno durante as partidas. Quando o aluno erra repetidamente a mesma habilidade, o sistema detecta a dificuldade, adapta os próximos desafios e avisa o professor.

Existem três perfis de uso:

| Perfil | O que faz |
|---|---|
| **Aluno** | Joga as trilhas, ganha estrelas, compra itens na loja e personaliza o personagem |
| **Professor** | Acompanha insights da turma, vê o perfil de cada aluno, liga ou desliga disciplinas e cria atividades |
| **Coordenador** | Vê indicadores da escola, cadastra professores e turmas e revisa os conteúdos criados |

## Arquitetura

```mermaid
flowchart LR
    A["App Android (Java)<br/>login nativo + telas em WebView"] -- "HTTP / JSON" --> B["API Spring Boot + JPA<br/>porta 8080"]
    B --> C[("MySQL 8<br/>educa_mais")]
```

O app não acessa o banco diretamente. Toda leitura e gravação passa pela API, que concentra as regras de negócio. A mesma API também serve as telas em `http://localhost:8080`, então dá para testar tudo pelo navegador.

## Estrutura do repositório

| Pasta | Conteúdo | Abrir com |
|---|---|---|
| [`banco/`](banco) | `01_schema.sql` (tabelas, gatilhos, procedure e views), `02_dados_demo.sql` (escola de demonstração) e `gerar_dados_demo.py` | MySQL Workbench |
| [`api/`](api) | API REST em Spring Boot 3 + JPA + MySQL | IntelliJ IDEA |
| [`android/`](android) | App Android em Java | Android Studio |
| [`Educa+_debug.apk`](Educa%2B_debug.apk) | App já compilado, para instalar direto no celular | — |

## Tecnologias

- **Banco:** MySQL 8, com gatilhos, procedure (`sp_comprar_item`) e views
- **API:** Java 17+, Spring Boot 3.3, Spring Data JPA, Maven (com wrapper)
- **App:** Java 17, Android Gradle Plugin 8.7.3 e Gradle 8.11.1 (compileSdk e targetSdk 35, minSdk 26), WebView, `HttpURLConnection` e `org.json`. Nenhuma biblioteca externa

## Como rodar

### Pré-requisitos

- MySQL 8 e MySQL Workbench
- JDK 17 ou 21 (veja a [observação sobre o JDK 25](#problemas-comuns))
- IntelliJ IDEA e Android Studio (ou o terminal, para a API)

### 1. Banco de dados

1. Abra o MySQL Workbench e conecte no seu servidor local.
2. Execute `banco/01_schema.sql` (**File → Open SQL Script** e depois o raio ⚡).
3. Execute `banco/02_dados_demo.sql` (leva cerca de 10 segundos).

Se já existir um banco `educa_mais` de versões antigas, apague antes:

```sql
DROP DATABASE educa_mais;
```

Os dados de demonstração incluem 8 turmas, 184 alunos, 5 professores e quase 6 mil partidas desde junho. As datas são relativas ao dia atual, então os painéis não envelhecem. Para gerar tudo de novo:

```bash
python banco/gerar_dados_demo.py
```

### 2. API

1. No IntelliJ, abra a pasta `api` (**File → Open**). Ele reconhece o Maven sozinho.
2. Informe o usuário e a senha do **seu** MySQL. A forma recomendada é por variáveis de ambiente, assim a senha não vai para o repositório:

   ```bash
   # Linux / macOS
   export DB_USER=root
   export DB_PASS=sua_senha

   # Windows (PowerShell)
   $env:DB_USER="root"
   $env:DB_PASS="sua_senha"
   ```

   No IntelliJ, também dá para definir em *Run → Edit Configurations → Environment variables*. Sem as variáveis, o padrão em `src/main/resources/application.properties` é `root` / `root`.

3. Rode a classe `EducaMaisApplication`.
4. Teste no navegador: <http://localhost:8080>

Sem IDE:

```bash
cd api
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

### 3. App Android

1. No Android Studio, abra a pasta `android` (**File → Open**) e aguarde o Gradle sincronizar.
2. **Emulador:** é só dar Run ▶. O app já aponta para `http://10.0.2.2:8080`, que é o computador visto de dentro do emulador.
3. **Celular de verdade:**
   - Celular e computador precisam estar na **mesma rede Wi-Fi**.
   - Descubra o IP do computador (no Windows, `ipconfig` e depois "Endereço IPv4", algo como `192.168.0.15`).
   - No app, toque em **Configurar servidor** e informe `http://192.168.0.15:8080`.
   - Se não conectar, libere o Java no Firewall do Windows em **Redes privadas**.
4. **Sem compilar:** envie o [`Educa+_debug.apk`](Educa%2B_debug.apk) para o celular e abra. O Android vai pedir permissão para instalar apps desconhecidos.

> **Por que precisa do IP?** `localhost` sempre significa "esta própria máquina". No celular ou no emulador, isso aponta para o próprio aparelho, e não para o computador onde a API está rodando. No emulador existe o atalho `10.0.2.2`. No celular físico, só o IP da rede funciona.

## Como o app funciona por dentro

- **`LoginActivity`** é a tela nativa (XML). Busca as contas de teste em `/api/auth/contas-teste` e faz login em `/api/auth/login`.
- **`MainActivity`** abre as telas do jogo (`assets/www/index.html`) num WebView e registra a ponte `EducaAndroid`.
- **`ApiClient`** faz as chamadas HTTP. Quando uma tela precisa de dados, o JavaScript chama `EducaAndroid.request(...)`, o Java faz o HTTP e devolve a resposta para a tela.
- O botão voltar do Android é repassado para as telas (fecha a folha aberta, sai do nível ou volta uma tela).
- No navegador (`http://localhost:8080`), as mesmas telas chamam a API direto com `fetch`. No app, a chamada passa pela ponte Java.
- O arquivo das telas é um só: `api/src/main/resources/static/index.html`. A cada build, o Gradle copia esse arquivo para dentro do app (tarefa `copiarTelas` em `android/app/build.gradle`).

## Contas de teste

| Conta | Perfil | O que tem |
|---|---|---|
| Lia Martins | Aluna, 4º A | Mapa com 6 ilhas, Nível 8 de Matemática liberado, 610 estrelas |
| João Pedro Alves | Aluno, 4º A | Sem a Ilha de História (desligada pela professora) |
| Carla Nogueira | Professora do 3º A, 4º A e 4º B | Insights, perfis, disciplinas por aluno, editor de atividades |
| Paulo Andrade | Coordenador | Indicadores, professores, turmas, revisão de conteúdos |

## Regras principais

- **Estrelas por nível:** 0 erros dá 3 estrelas, até 2 erros dá 2 e mais que isso dá 1. Ao gravar a partida, um gatilho do banco credita **10 estrelas de saldo para cada estrela** conquistada.
- **Loja:** a compra passa pela procedure `sp_comprar_item`, que confere se o item existe, se o aluno já o tem e se há saldo, tudo numa transação. Erros como "Estrelas insuficientes" voltam para o app como mensagem.
- **Dificuldade:** ao errar 2 vezes a mesma habilidade, o jogo registra a dificuldade na API, que cria um insight para o professor. Em Matemática, os próximos desafios daquela habilidade são trocados por versões mais fáceis. Ao passar no treino especial, a dificuldade vira "superada" e a API gera um insight de avanço.
- **Revisão de conteúdo:** questões criadas por professores entram como **Em revisão** e só ficam publicadas depois que o coordenador aprova. Questões criadas pelo coordenador já entram publicadas.
- **Insights:** a API gera insights para professores e para a coordenação a partir dos dados (tipos: dificuldade, avanço, atenção e recomendação). Eles são agrupados por turma e não se repetem em 30 dias.

## O que é real e o que é simplificado

- **Dados:** tudo que aparece vem do MySQL, e tudo que se faz é gravado (partidas, estrelas, compras, personagem, dificuldades, insights, níveis, questões, turmas, professores e disciplinas).
- **IA:** é feita por **regras**, sem modelo de machine learning.
  - No jogo: 2 erros na mesma habilidade registram a dificuldade e avisam o professor. Em Matemática, os próximos desafios daquela habilidade ficam mais fáceis.
  - Na API (`IaService` e `BootstrapService`): agrupa erros, acertos e inatividade por turma e gera os insights.
- **Tico (chat):** responde com roteiros por habilidade, sem gerar texto com IA. As conversas ficam gravadas em `mensagem_tutor`.
- **Login Google:** por enquanto são contas de teste. Para ligar o Google de verdade é preciso criar um projeto no Google Cloud/Firebase com o SHA-1 do Android Studio. A API já tem a coluna `google_sub` e o endpoint de login esperando o e-mail confirmado.
- **Segurança:** não há autenticação nas chamadas da API, então qualquer pessoa na rede consegue chamá-la. A API escuta em todas as interfaces (`0.0.0.0`, para o celular acessar), o CORS está aberto, o app permite tráfego HTTP sem criptografia e a senha padrão do banco é `root`. Serve para demonstração em rede local, **não para produção**.
- **Minijogos:** os desafios das trilhas são gerados pelo motor do jogo. O banco guarda as questões criadas por professores (tabelas `questao` e `alternativa`) e o resultado de cada partida.

## Endpoints da API

| Método | Caminho | Para quê |
|---|---|---|
| GET | `/api/auth/contas-teste` | Contas para a tela de login |
| POST | `/api/auth/login` | Entrar com e-mail |
| GET | `/api/bootstrap/{idUsuario}` | Tudo que as telas do perfil precisam |
| POST | `/api/alunos/{id}/partidas` | Grava uma partida (o gatilho credita as estrelas) |
| POST | `/api/alunos/{id}/dificuldades` | IA detectou dificuldade |
| POST | `/api/alunos/{id}/dificuldades/resolver` | Aluno passou no treino |
| POST | `/api/alunos/{id}/compras` | Compra na loja (procedure) |
| PUT | `/api/alunos/{id}/personagem` | Salva cores e itens usados |
| POST | `/api/alunos/{id}/mensagens` | Conversa com o Tico |
| PUT | `/api/alunos/{id}/disciplinas/{codigo}` | Liga ou desliga uma ilha para o aluno |
| GET | `/api/alunos/{id}/detalhe` | Erros e acertos por habilidade |
| POST | `/api/alunos` | Cadastra aluno |
| POST / PUT | `/api/niveis`, `/api/niveis/{id}` | Cria e edita níveis |
| PUT | `/api/trilhas/{ano}/{codigo}/ordem` | Reordena a trilha |
| POST / PUT | `/api/questoes`, `/api/questoes/{id}` | Cria questão e muda a situação (revisão) |
| POST | `/api/professores`, `/api/turmas` | Cadastros da coordenação |
| PUT | `/api/disciplinas/{codigo}/anos` | Em quais anos a escola oferece a disciplina |
| PUT | `/api/insights/{id}` | Marca insight como feito ou dispensado |

## Banco de dados

O schema fica em `banco/01_schema.sql`. As mudanças da versão 3 estão marcadas com `[v3]`:

- Novas colunas: `usuario.ultimo_acesso`, `disciplina.codigo`, `atividade.tipo_minijogo`, `atividade.id_habilidade`, `realizacao_atividade.acertos` e `insight_ia.id_aluno`.
- 43 habilidades cadastradas, além dos itens da loja e das conquistas.

O JPA está com `ddl-auto=none`: as tabelas são criadas só pelo script SQL, porque ele inclui gatilhos, procedure e views.

## Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `Access denied for user 'root'` ao subir a API | Usuário ou senha do MySQL errados. Confira `DB_USER` e `DB_PASS` |
| `Unknown database educa_mais` | Rode os dois scripts da pasta `banco/` antes de subir a API |
| App mostra "Sem conexão com…" | A API está rodando? No celular, use o IP do computador (não `localhost`) e a mesma Wi-Fi |
| `ExceptionInInitializerError` / `TypeTag :: UNKNOWN` ao compilar a API | O Lombok do Spring Boot 3.3.5 não suporta o JDK 25. Use o **JDK 17 ou 21** ou force `<lombok.version>1.18.42</lombok.version>` em `<properties>` do `pom.xml` |
| Android Studio: "Gradle JVM incompatível" | O Gradle 8.11 aceita JVM de 8 a 23. Em *Settings → Build Tools → Gradle → Gradle JDK*, escolha o **JBR 17 ou 21** embutido no Studio |
| Android Studio pede para atualizar o Gradle ou o AGP | Pode aceitar |
| Aviso `restricted method in java.lang.System` no log | Só um aviso do JDK 24+ sobre o Tomcat. Não afeta nada. Para sumir, use o JDK 17/21 ou adicione `--enable-native-access=ALL-UNNAMED` nas VM options |

## Autores

<!-- Adicione aqui os nomes da equipe e, se quiser, o link do GitHub de cada um. -->

## Licença

<!-- Se quiser deixar o projeto aberto, adicione um arquivo LICENSE (por exemplo MIT) e cite aqui. -->

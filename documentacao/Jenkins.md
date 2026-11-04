# Pipeline de CI/CD com Jenkins

Este guia explica como instalar o Jenkins no Windows e rodar a pipeline de CI/CD do projeto na sua própria máquina. A pipeline está definida no arquivo `Jenkinsfile`, na raiz do repositório.

## O que a pipeline faz

Para cada branch que tem um `Jenkinsfile`, o Jenkins:

1. Baixa o código do GitHub (**Checkout SCM**).
2. Instala o Maven configurado como `Maven3` (**Tool Install**).
3. Compila e empacota o projeto com `mvn -B clean package -DskipTests` (**Build**).
4. Roda os testes e gera os relatórios de testes e de cobertura com `mvn -B verify` (**Test**).
5. Reprova o build se a cobertura de linhas ficar abaixo de 70% com `mvn -B jacoco:check` (**Cobertura**).
6. Dispara uma requisição webhook para atualizar a aplicação na nuvem (**Deploy**).
7. Publica os resultados dos testes, a cobertura e o relatório HTML do JaCoCo, mesmo se algum stage falhar (**Post Actions**).

Cada stage só roda se o anterior passou. Ou seja, o Deploy só acontece se o build, os testes e a cobertura estiverem 100% aprovados.

## Pré-requisitos

- Windows
- Git
- **JDK 21** (o Jenkins exige Java 21 ou 25; o Java 17 não funciona para rodar o servidor do Jenkins)

## 1. Instalar o JDK 21

1. Baixe o **Temurin 21 (LTS)**, formato `.msi`, em <https://adoptium.net/temurin/releases/?version=21>.
2. Na tela **Custom Setup** do instalador, ative:
   - **Modify PATH variable**
   - **Set or override JAVA_HOME variable** (clique no ícone e escolha *Will be installed on local hard drive*)
3. Conclua a instalação.

## 2. Instalar o Jenkins

1. Baixe o instalador `.msi` da versão **LTS** para Windows em <https://www.jenkins.io/download/>.
2. Durante a instalação:
   - **Logon Type:** marque **Run service as LocalSystem**.
   - **Porta:** deixe `8080`.
   - **Java:** aponte para a pasta do JDK 21 (a pasta base do JDK, não a `bin`).
   - **Firewall Exception:** pode deixar desmarcado.
3. Finalize a instalação.

## 3. Configuração inicial

1. Abra `http://localhost:8080`.
2. A senha inicial está em: `C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword`
3. Escolha **Instalar as extensões sugeridas**.
4. Crie o seu usuário administrador e conclua.

## 4. Configurar o Maven no Jenkins

> **Atenção:** o nome precisa ser exatamente `Maven3`, igual ao que está no `Jenkinsfile`.

1. Vá em **Gerenciar Jenkins > Tools**.
2. Em **Maven installations**, clique em **Add Maven**.
3. Nome: `Maven3`.
4. Marque **Install automatically** e clique em **Save**.

## 5. Plugins

Obrigatórios em **Gerenciar Jenkins > Plugins**:
- **JUnit**: Mostra o resultado dos testes no build.
- **Coverage**: Mostra a cobertura do JaCoCo (Não vem nas extensões sugeridas, precisa instalar).

## 6. O `Jenkinsfile` e o Deploy na Nuvem (Render)

A tabela de execução no Jenkinsfile segue esta ordem:

| Stage | Comando | Falha quando |
|---|---|---|
| Build | `mvn -B clean package -DskipTests` | O código não compila. |
| Test | `mvn -B verify` | Algum teste falha. |
| Cobertura | `mvn -B jacoco:check` | A cobertura de linhas fica abaixo de 70%. |
| Deploy | `curl -X POST ...` | O Jenkins não consegue acionar a API da nuvem. |

### Deploy via Render
A aplicação é construída e hospedada em nuvem através do **Render**. Para que isso funcione, utilizamos um arquivo `Dockerfile` na raiz do projeto (que constrói a imagem Java 21) e um *Deploy Hook*.

O Jenkins apenas sinaliza o Render de que novos testes foram aprovados. Ele faz isso enviando uma requisição HTTP.

### Configurando o Deploy Hook no Jenkins
Para que o Jenkins do seu computador consiga enviar esse aviso para a nuvem:
1. Vá em **Gerenciar Jenkins > System**.
2. Na seção **Global properties**, marque **Environment variables**.
3. Clique em **Add** e crie a variável:
   - **Name:** `RENDER_DEPLOY_HOOK`
   - **Value:** Cole a URL fornecida no painel do Render (em Settings > Deploy Hook).
4. Clique em **Save**.

*Nota:* Se essa variável não estiver configurada no seu Jenkins, o stage de Deploy será pulado e o pipeline terminará como sucesso apenas com a fase de testes local.

### Banco de Dados (`DB_URL`)
Como a aplicação agora roda em nuvem, as variáveis de conexão com o banco de dados (como `DB_URL`, usuário e senha) **NÃO** devem ser cadastradas no Jenkins nem devem ir para o GitHub.
Elas devem ser configuradas exclusivamente no painel do Render, na aba **Environment**, onde a aplicação executará de fato.

## 7. Criar o job no Jenkins

1. No Jenkins, clique em **Nova tarefa**.
2. Dê um nome, escolha **Multibranch Pipeline** e clique em **OK**.
3. Em **Branch Sources > Git**:
   - **Project Repository:** `https://github.com/C14-Inatel-2026-2/Prestador_de_Servico`
   - **Credentials:** `none`
4. Em **Build Configuration**, deixe **by Jenkinsfile**.
5. Salve. O Jenkins fará o primeiro scan.

## 8. Regras do Grupo

1. Trabalhe em branch própria.
2. Todo pull request precisa de uma captura de tela do gráfico verde de aprovação do Jenkins na descrição.
3. Use a padronização semântica (ex: `feat:`, `fix:`, `ci:`, `build:`).
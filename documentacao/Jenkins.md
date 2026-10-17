# Pipeline de CI/CD com Jenkins

Este guia explica como instalar o Jenkins no Windows e rodar a pipeline de CI/CD do projeto na sua própria máquina. A pipeline está definida no arquivo `Jenkinsfile`, na raiz do repositório.

## O que a pipeline faz

Para cada branch que tem um `Jenkinsfile`, o Jenkins:

1. Baixa o código do GitHub (**Checkout SCM**).
2. Instala o Maven configurado como `Maven3` (**Tool Install**).
3. Compila e empacota o projeto com `mvn -B clean package -DskipTests` (**Build**).
4. Roda os testes e gera os relatórios de testes e de cobertura com `mvn -B verify` (**Test**).
5. Reprova o build se a cobertura de linhas ficar abaixo de 70% com `mvn -B jacoco:check` (**Cobertura**).
6. Implanta o `.jar` aprovado no ambiente de execução e roda um smoke test (**Deploy**).
7. Publica os resultados dos testes, a cobertura e o relatório HTML do JaCoCo, mesmo se algum stage falhar (**Post Actions**).

Cada stage só roda se o anterior passou. Ou seja, o Deploy só acontece se o build, os testes e a cobertura estiverem aprovados.

Não é necessário Docker nem conta no site do Jenkins. Tudo roda localmente em `http://localhost:8080`.

## Pré-requisitos

- Windows
- Git
- **JDK 21** (o Jenkins 2.580 exige Java 21 ou 25; o Java 17 não funciona para o Jenkins)

> O projeto pode continuar compilando com Java 17. O JDK 21 é necessário só para o próprio Jenkins rodar.

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
    - **Java:** aponte para a pasta do JDK 21, por exemplo `C:\Program Files\Eclipse Adoptium\jdk-21...-hotspot\` (a pasta do JDK, não a `bin`).
    - **Firewall Exception:** pode deixar desmarcado.
3. Finalize a instalação.

## 3. Configuração inicial

1. Abra `http://localhost:8080` (se aparecer "Jenkins is getting ready", aguarde um minuto e atualize).
2. A senha inicial está em:
   ```
   C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword
   ```
   Abra o arquivo no Bloco de Notas e cole a senha.
3. Escolha **Instalar as extensões sugeridas** e aguarde.
4. Crie o seu usuário administrador (usuário e senha locais, só valem na sua máquina).
5. Mantenha a URL `http://localhost:8080/` e conclua.

## 4. Configurar o Maven no Jenkins

> **Atenção:** o nome precisa ser exatamente `Maven3`, igual ao que está no `Jenkinsfile`. Qualquer outro nome faz o build falhar com "tool not found".

1. Vá em **Gerenciar Jenkins > Tools**.
2. Em **Maven installations**, clique em **Add Maven**.
3. Nome: `Maven3`.
4. Marque **Install automatically** e clique em **Save**.

Isso é feito no Jenkins (navegador), não no IntelliJ. O Maven embutido do IntelliJ não é visto pelo Jenkins.

## 5. Plugins

Confira em **Gerenciar Jenkins > Plugins > Installed plugins**. O que faltar, instale em **Available plugins**.

| Plugin | Obrigatório? | Para que serve |
|---|---|---|
| **JUnit** | Sim | Passo `junit` do `Jenkinsfile`: mostra o resultado dos testes no build. |
| **Coverage** | Sim | Passo `recordCoverage` do `Jenkinsfile`: mostra a cobertura do JaCoCo no build. Não vem nas extensões sugeridas. |
| **Pipeline Graph View** | Não | Mostra o gráfico de stages, parecido com o GitHub Actions. Costuma vir nas extensões sugeridas. |

Sem os plugins obrigatórios, o build termina com erro do tipo `No such DSL method`.

## 6. O `Jenkinsfile`

O arquivo está na raiz do repositório e tem estes stages:

| Stage | Comando | Falha quando |
|---|---|---|
| Build | `mvn -B clean package -DskipTests` | O código não compila. |
| Test | `mvn -B verify` | Algum teste falha. |
| Cobertura | `mvn -B jacoco:check` | A cobertura de linhas fica abaixo de 70% (regra no `pom.xml`). |
| Deploy | cópia do `.jar` + `java -jar` | O `.jar` não é copiado ou não executa. |

Alterações no `Jenkinsfile` devem ser feitas por pull request, para ninguém quebrar o arquivo que todo o grupo usa.

### Deploy

A aplicação ainda é de console e não tem servidor, então o ambiente de execução é simulado por uma pasta fora do workspace, na máquina do Jenkins:

- Windows: `C:\ProgramData\Jenkins\.jenkins\deploy\prestador-de-servicos\prestador-de-servicos.jar`
- Linux/Mac: `$JENKINS_HOME/deploy/prestador-de-servicos/prestador-de-servicos.jar`

O stage copia para essa pasta o `.jar` que passou nos testes e na cobertura e roda `java -jar` nele como smoke test. Se o `.jar` implantado não executar, o build fica vermelho. A pasta é a mesma para todas as branches, então ela sempre guarda o último build aprovado.

Para o `java -jar` funcionar, o `pom.xml` define a classe principal (`br.inatel.engsoftware.Main`) no manifesto do jar, pelo `maven-jar-plugin`.

Para conferir o deploy: abra o **Console Output** do build e procure a mensagem `Build #N implantado em ...`. Ou rode o `.jar` implantado direto no terminal:

```
java -jar "C:\ProgramData\Jenkins\.jenkins\deploy\prestador-de-servicos\prestador-de-servicos.jar"
```

## 7. Criar o job no Jenkins

1. Na tela inicial, clique em **Novo tarefa**.
2. Dê um nome (por exemplo `Projeto_Eng_Software`), escolha **Multibranch Pipeline** e clique em **OK**.
3. Em **Branch Sources**, clique em **Add source > Git**:
    - **Project Repository:** `https://github.com/C14-Inatel-2026-2/Prestador_de_Servico`
    - **Credentials:** `- none -` (o repositório é público)
    - **Behaviours:** deixe **Discover branches** (sem filtro, para o Jenkins achar a branch de cada pessoa).
4. Em **Build Configuration**, deixe **by Jenkinsfile** com Script Path `Jenkinsfile`.
5. Em **Scan Multibranch Pipeline Triggers**, marque **Periodically if not otherwise run** e escolha um intervalo curto (por exemplo, 5 minutes).
6. Clique em **Save**. O Jenkins faz o primeiro scan sozinho.

## 8. Ver o resultado

1. Abra o job e clique na sua branch.
2. Clique na execução (`#1`, `#2`...).
3. O gráfico de stages deve ficar todo verde. Para ver o log completo, clique em **Console Output**; no final deve aparecer `BUILD SUCCESS` e `Finished: SUCCESS`.

O Jenkins só cria o job de uma branch que **já tem o `Jenkinsfile`**. Se a sua branch foi criada antes de o arquivo chegar na `main`, traga-o para ela:

```bash
git checkout sua-branch
git merge main
git push
```

Depois clique em **Scan Repository Now** no job.

## Fluxo de trabalho do grupo

1. Cada pessoa trabalha na própria branch.
2. Antes de abrir o pull request, confira que o build da sua branch ficou **verde** no Jenkins.
3. Cole um print do gráfico de stages na descrição do PR.
4. O PR só deve ser aprovado com o build verde.
5. Depois do merge, o `Jenkinsfile` e as mudanças ficam na `main`. Clique em **Scan Repository Now** para o Jenkins criar o job da `main`.

## Rodar a pipeline só na main (opcional)

Se o grupo decidir que o Jenkins deve construir apenas a `main`, existem duas formas. Em ambas, o `Jenkinsfile` precisa estar na `main`.

> **O que muda:** com só a `main`, nada é construído antes do merge. Para testar sua branch antes do pull request, rode o build localmente pelo painel **Maven** do IntelliJ (**Lifecycle > clean** e **package**), ou remova o filtro temporariamente.

### Opção A: manter o job Multibranch e filtrar a main (mais simples)

1. Abra o job no Jenkins e clique em **Configurar** (menu da esquerda).
2. Em **Branch Sources > Git > Behaviours**, clique em **Add** e escolha **Filter by name (with wildcards)**.
3. Em **Include**, escreva `main`. Deixe **Exclude** vazio.
4. Clique em **Save** e depois em **Scan Repository Now**.

As outras branches deixam de ser construídas e saem da lista do job (ou ficam marcadas como desativadas). Para voltar a construir todas as branches, remova o filtro com o **X** vermelho ao lado dele e salve de novo.

### Opção B: trocar por um job Pipeline simples

Use esta opção se quiser abandonar o Multibranch de vez.

1. Na tela inicial, clique em **Novo tarefa**, dê um nome e escolha **Pipeline** (não o Multibranch).
2. Em **Build Triggers**, marque **Poll SCM** e coloque `H/5 * * * *` para o Jenkins verificar o GitHub a cada 5 minutos (opcional).
3. Em **Pipeline**, escolha **Definition: Pipeline script from SCM**.
4. **SCM:** Git. **Repository URL:** `https://github.com/C14-Inatel-2026-2/Prestador_de_Servico`. **Credentials:** `- none -`.
5. **Branch Specifier:** `*/main`.
6. **Script Path:** `Jenkinsfile`.
7. Clique em **Save** e depois em **Construir agora**.

O gráfico de stages e o **Console Output** funcionam igual aos do Multibranch. Depois de criar o job novo, o job Multibranch antigo pode ser apagado ou desativado.

## Problemas comuns

| Problema | Causa e solução |
|---|---|
| Instalador do Jenkins diz "Failed to find compatible Java version (21 or 25)" | Instale o JDK 21 e aponte para a pasta dele no instalador. |
| Build falha com "tool not found" ou similar | O Maven não foi cadastrado como `Maven3` em **Gerenciar Jenkins > Tools** (o nome precisa ser idêntico). |
| `mvn` não é reconhecido no terminal do Windows | Normal. O Maven não está no PATH do Windows e isso não afeta o Jenkins. Para testar localmente, use o painel **Maven** do IntelliJ. |
| O scan não encontra sua branch | O `Jenkinsfile` não está nela ou o push não foi feito. Confira no GitHub e use `git merge main` se for o caso. |
| A `main` não aparece no Jenkins | O `Jenkinsfile` ainda não chegou na `main`. Depois do merge do PR, clique em **Scan Repository Now**. |
| Porta 8080 em uso | Troque a porta no instalador (por exemplo, 8081) ou feche o programa que está usando. |
| Novos pushes não disparam build sozinhos | O Jenkins roda em `localhost` e o GitHub não consegue avisá-lo. Use o scan periódico ou **Scan Repository Now**. |
| `No such DSL method 'recordCoverage'` ou `'junit'` | Falta o plugin **Coverage** ou **JUnit**. Veja a seção 5. |
| Stage Deploy falha com `'java' não é reconhecido` | O serviço do Jenkins não enxerga o Java no PATH. Reinicie o serviço do Jenkins ou troque `java` por `"%JAVA_HOME%\bin\java"` no stage Deploy. |
| Stage Deploy falha com `no main manifest attribute` | O `pom.xml` da sua branch está sem a configuração do `maven-jar-plugin`. Traga a `main` para a sua branch com `git merge main`. |

## Próximos passos

Aviso para o próximo membro que for mexer na pipeline:

- **Deploy externo.** Hoje o Deploy é local. Quando houver um servidor ou uma interface, troque só o stage Deploy (por exemplo, publicando o `.jar` no GitHub Releases ou em um servidor). Os outros stages continuam iguais.

- **Manter esta documentação atualizada.** Sempre que o `Jenkinsfile` ou o processo de configuração mudar (novo stage, nova versão do Java, mudança no job), atualize este arquivo no mesmo pull request.

# Pipeline de Build com Jenkins

Este guia explica como instalar o Jenkins no Windows e rodar a pipeline de build do projeto na sua própria máquina. A pipeline está definida no arquivo `Jenkinsfile`, na raiz do repositório.

## O que a pipeline faz

Para cada branch que tem um `Jenkinsfile`, o Jenkins:

1. Baixa o código do GitHub (**Checkout SCM**).
2. Instala o Maven configurado como `Maven3` (**Tool Install**).
3. Compila e empacota o projeto com `mvn -B clean package -DskipTests` (**Build**).
4. Imprime `Build OK` ou `Build falhou` (**Post Actions**).

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

## 5. Visual de stages (opcional)

O plugin **Pipeline Graph View** mostra o gráfico de stages, parecido com o GitHub Actions. Ele costuma vir junto com as extensões sugeridas. Para conferir, vá em **Gerenciar Jenkins > Plugins > Installed plugins**.

## 6. O `Jenkinsfile`

O arquivo está na raiz do repositório:

```groovy
pipeline {
    agent any
    tools { maven 'Maven3' }
    stages {
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean package -DskipTests'
                    } else {
                        bat 'mvn -B clean package -DskipTests'
                    }
                }
            }
        }
    }
    post {
        success { echo 'Build OK' }
        failure { echo 'Build falhou' }
    }
}
```

Alterações no `Jenkinsfile` devem ser feitas por pull request, para ninguém quebrar o arquivo que todo o grupo usa.

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

## Próximos passos

Aviso para o próximo membro que for mexer na pipeline:

- **Rodar os testes na pipeline.** Hoje o stage Build usa `-DskipTests` de propósito: a pipeline valida só a compilação e o empacotamento. Quem for incluir os testes deve criar um `stage('Test')` depois do Build, com `mvn -B test` (e, se quiser, publicar o relatório do JaCoCo). O `-DskipTests` pode continuar no Build, já que os testes passam a rodar no stage separado.

```groovy
stage('Test') {
    steps {
        script {
            if (isUnix()) {
                sh 'mvn -B test'
            } else {
                bat 'mvn -B test'
            }
        }
    }
}
```

- **Manter esta documentação atualizada.** Sempre que o `Jenkinsfile` ou o processo de configuração mudar (novo stage, nova versão do Java, mudança no job), atualize este arquivo no mesmo pull request.

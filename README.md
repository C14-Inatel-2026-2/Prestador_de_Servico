# 🛠️ Prestador de Serviços

![Disciplina](https://img.shields.io/badge/Disciplina-C14%20Engenharia%20de%20Software-blue)
![Licença](https://img.shields.io/badge/Licença-MIT-green)

> Projeto desenvolvido para a disciplina **C14 — Engenharia de Software**, focado em conectar clientes e profissionais de forma simples.

---

## 🎯 Finalidade

Contratar um serviço de manutenção doméstica ou de obras, como pedreiros autônomos, ainda depende de indicações ou divulgações informais. Por exemplo,quem precisa de um encanador não tem como comparar preço e não tem como saber se o profissional é confiável e realiza um bom trabalho. Do outro lado, o prestador autônomo depende do boca a boca e não tem onde mostrar o próprio histórico de trabalho. Esse cenário dificulta a busca por esses profissionais e a divulgaão dos seus serviços.

Diante disso, o objetivo do projeto é resolver ambos os lados desse problema: fornecer ao contratante um lugar para encontrar esses serviços e comparar prestadores e ao prestador um lugar para ser encontrado e construir reputação.

---

## 💡 Ideia Principal
Uma aplicação de cadastro e busca de prestadores de serviço como:  
* 🧱 Pedreiros
* 🔧 Encanadores
* ⚡ Eletricistas
* 🪚 Montadores de móveis

O contratante busca pela profissão que precisa e verifica os prestadores disponíveis com o valor da hora ou do serviço específico e a nota média do profissional. Compara, escolhe e depois deixa uma avaliação com nota de 1 a 5 e comentário. Cada avaliação entra no cálculo da nota média que é o que aparece para o próximo contratante.

O prestador cadastra os serviços que oferece e o valor que cobra, e acompanha as avaliações que recebeu.

A avaliação vale para os dois lados: prestador também avalia o contratante

---

## 📋 Requisitos Funcionais
Os requisitos funcionais descrevem as funcionalidades essenciais que o sistema disponibiliza aos contratantes e prestadores de serviços.

| Código | Requisito | Descrição |
| :--- | :--- | :--- |
| **RF01** | Cadastro de Contratantes | Permite que novos contratantes realizem seu cadastro, informando os dados necessários para a criação de uma conta. |
| **RF02** | Cadastro de Prestadores | Permite que profissionais realizem seu cadastro na plataforma, registrando suas informações. |
| **RF03** | Autenticação de Usuários | Permite que contratantes e prestadores realizem login utilizando suas credenciais cadastradas. |
| **RF04** | Alteração de Senha | Permite que o usuário altere sua senha de acesso, mediante validação de sua senha atual. |
| **RF05** | Cadastro de Serviços | Permite que os prestadores cadastrem os serviços que oferecem, informando os respectivos valores cobrados. |
| **RF06** | Pesquisa de Prestadores | Permite que os contratantes pesquisem prestadores de acordo com a profissão ou categoria de serviço desejada. |
| **RF07** | Consulta de Informações | Apresenta informações dos prestadores cadastrados, incluindo serviços oferecidos, valores cobrados e notas médias. |
| **RF08** | Avaliação de Prestadores | Permite que os contratantes avaliem os prestadores após um serviço, atribuindo nota de 1 a 5 e comentário. |
| **RF09** | Atualização da Nota Média | Atualiza automaticamente a nota média do prestador considerando as avaliações recebidas. |
| **RF10** | Consulta de Avaliações Recebidas | Permite que os prestadores consultem as avaliações realizadas pelos contratantes. |
| **RF11** | Avaliação de Contratantes | Permite que os prestadores registrem avaliações dos contratantes, possibilitando avaliação mútua. |
| **RF12** | Histórico de Avaliações | Permite que os contratantes consultem as avaliações que já realizaram para os prestadores. |

---

## 🛡️ Requisitos Não Funcionais

Os requisitos não funcionais descrevem características de qualidade, segurança e desempenho que devem ser consideradas no desenvolvimento e funcionamento da aplicação.

| Código | Requisito | Descrição |
| :--- | :--- | :--- |
| **RNF01** | Segurança de Senhas | As senhas dos usuários devem ser armazenadas utilizando mecanismos seguros de hash, evitando o armazenamento de senhas em texto puro. |
| **RNF02** | Integridade dos Dados | O sistema deve preservar a consistência das informações cadastradas, garantindo que as avaliações estejam corretamente associadas aos respectivos contratantes e prestadores. |
| **RNF03** | Persistência dos Dados | As informações cadastradas devem ser preservadas após o encerramento da aplicação, permitindo sua recuperação em execuções posteriores. |
| **RNF04** | Usabilidade | O sistema deve apresentar funcionalidades e mensagens de retorno de maneira clara, facilitando a utilização por contratantes e prestadores. |
| **RNF05** | Desempenho | As operações de pesquisa e consulta de prestadores e serviços devem ser executadas de maneira eficiente, evitando demora desnecessária na apresentação dos resultados. |
| **RNF06** | Manutenibilidade | O código-fonte deve ser organizado e estruturado de maneira que facilite a manutenção, a identificação de problemas e a implementação de novas funcionalidades. |
| **RNF07** | Confiabilidade | O sistema deve tratar situações de erro e entradas inválidas, evitando comportamentos inesperados durante a utilização das funcionalidades. |

---

## 📊 Organização e Gerenciamento do Projeto
O desenvolvimento do projeto é organizado utilizando a metodologia ágil Scrum, com o apoio da ferramenta Jira.

O Jira é utilizado para organizar o Product Backlog, cadastrar as histórias de usuário, definir critérios de aceitação, atribuir responsáveis e planejar as atividades das Sprints.

Cada integrante participa da elaboração das histórias de usuário e da organização das atividades, contribuindo para o desenvolvimento colaborativo da aplicação.

[![Acessar Board no Jira](https://img.shields.io/badge/Jira-Acessar%20Quadro%20Scrum-0052CC?style=for-the-badge&logo=jira&logoColor=white)](https://projeto-eng-de-software.atlassian.net/jira/software/projects/SCRUM/boards/1?filter=&groupBy=none)
---

## 🔄 Refactorings

Nesta seção estão documentadas as melhorias realizadas no código.


### 1.  Padronização na ordem dos argumentos dos construtores para as classes Usuário/Prestador/Contratante

> Correção nos construtores colocando o atributo cidade na mesma ordem para os dois, para padronização na ordem dos argumentos dos construtores.

* **Code Smell:** Inconsistência na ordem dos parâmetros de construtores da hierarquia Usuario/Prestador/Contratante (parâmetros do mesmo tipo String em posições trocadas).
* **Refactoring aplicado:** Reordenação e padronização dos argumentos nos construtores, mudanças puramente estruturais sem alterar o comportamento externo.
* **Por quê essa alteração foi feita?:** Evitar atribuição cruzada de dados (cidade x telefone/profissao) e alinhar a assinatura da superclasse Usuario com suas subclasses herdadas e evitar que os atributos sejam persistidos ou recuperados incorretamente.
* **Link do PR:** [#71 - Padronização dos construtores](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/pull/71)

### 2. Extração do método de validação de preço na classe Servico

> Centralizar a validação do preço sugerido de um serviço, eliminando a duplicação de código entre o construtor e o método setter.

- **Code Smell:** Código duplicado, a mesma lógica de validação do preço sugerido estava repetida no construtor e no método `setPrecoSugerido()` da classe `Servico`.

- **Refactoring aplicado:** Extração da lógica de validação para um método privado `validarPreco()`, reutilizado pelo construtor e pelo setter.

- **Por que essa alteração foi feita?:** Eliminar código duplicado, facilitar a manutenção e centralizar a validação do preço sugerido, garantindo que ambos os métodos utilizem a mesma lógica. Essa alteração é puramente estrutural e preserva o comportamento original, sem modificar as classes relacionadas ou a persistência no banco.

- **Link do PR:** [#75 - Extração de Método](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/pull/75)

### 3. Extração da validação comum de `cadastrarServico` e `removerServico` na classe Prestador

> Os dois métodos começavam com as mesmas duas verificações antes de acessar o banco.

- **Code Smell:** Código duplicado. `cadastrarServico(int, double)` e `removerServico(int)` repetiam a checagem de que o prestador já está cadastrado (`id > 0`) e de que o ID do serviço é positivo.

- **Refactoring aplicado:** Extract Method. As duas verificações foram para o método privado `validarServicoDoPrestador(int idServico)`, chamado no início dos dois métodos.

- **Por que essa alteração foi feita?:** Com a regra em um lugar só, uma mudança futura (por exemplo, outra mensagem de erro) é feita uma vez e vale para os dois métodos. Os testes existentes do `Prestador` passaram antes e depois sem nenhuma alteração, o que mostra que o comportamento foi preservado.

- **Link do PR:** [#73 - Validação comum no Prestador](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/pull/73)

### 4. Extração da impressão dos serviços no `exibirPerfil` da classe Prestador

> O método misturava a impressão dos dados do perfil com a consulta ao banco e o tratamento de falha.

- **Code Smell:** Long Method, com níveis de abstração misturados. O `exibirPerfil()` imprimia os campos do perfil e, no meio, tinha um `if`, um `try/catch` e um laço que consultava e listava os serviços do banco.

- **Refactoring aplicado:** Extract Method. O trecho dos serviços virou o método privado `imprimirServicosCadastrados()`, com uma *guard clause* (`if (getId() <= 0) return;`) no lugar do `if` que envolvia todo o bloco.

- **Por que essa alteração foi feita?:** O `exibirPerfil()` passou a ler como uma lista do que é exibido, e a regra "só prestador salvo no banco tem serviços; se o banco falhar, mostra um aviso" ficou isolada e com um nome que a explica. A saída na tela continua a mesma.

- **Link do PR:** [#80 - Refatorações no Prestador](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/pull/80)

### 5. Extração do tratamento de `SQLException` na classe Prestador

> Três métodos repetiam o mesmo `try/catch` que transforma erro de banco em `IllegalStateException`.

- **Code Smell:** Código duplicado. `cadastrarServico`, `removerServico` e `listarServicosCadastrados` tinham, cada um, um bloco `try { ... } catch (SQLException e) { throw new IllegalStateException(mensagem, e); }` igual, mudando só a mensagem.

- **Refactoring aplicado:** Extract Method com interfaces funcionais. O `try/catch` foi para os métodos privados `consultarNoBanco` (operações que devolvem valor) e `executarNoBanco` (operações sem retorno). Cada método público passa só a mensagem de erro e a operação, como lambda. Foram criadas duas interfaces funcionais privadas que aceitam `throws SQLException`, porque as interfaces prontas do Java (`Supplier`, `Runnable`) não permitem exceção checada.

- **Por que essa alteração foi feita?:** A regra "erro de banco vira `IllegalStateException`, com mensagem e causa original" ficou num único lugar. Os testes do `PrestadorFalhasBancoTest` conferem, para os três métodos, a mensagem e que a `SQLException` original continua como causa (`assertSame`), o que garante que o comportamento não mudou.

- **Link do PR:** [#80 - Refatorações no Prestador](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/pull/80)

### 6. Tratamento de erros centralizado no menu (`Menu`)

> Cada opção do menu repetia o mesmo bloco de `try/catch`. O tratamento foi concentrado em um único método.

- **Code Smell:** Código duplicado. Cada opção do menu repetia `try/catch` para `IllegalArgumentException`, `IllegalStateException` e `SQLException`, com mensagens diferentes em cada lugar.

- **Refactoring aplicado:** Extract Method com interface funcional. Foi criado o método `executarAcao(Acao)`, que recebe a ação como parâmetro (interface funcional `Acao`, que aceita `throws SQLException`) e concentra o tratamento de erros em um só lugar.

- **Por que essa alteração foi feita?:** A regra de tratamento de erros passou a existir em um só ponto. Alterar uma mensagem ou o comportamento após um erro não exige mexer em várias opções, e cada ação fica apenas com a sua lógica.

- - **Link do PR:** [Commit e941d1d - Refatorações do menu](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/commit/e941d1d03e7d4f19b353e49f97c7cb3fe6ee5c2b)

### 7. Separação do menu principal em métodos por perfil (`Menu`)

> O método `executar()` concentrava o menu principal, o cadastro, o login e as opções de cada perfil em um único `switch`.

- **Code Smell:** Método longo (Long Method) e responsabilidades misturadas. A navegação de cada perfil estava dentro do mesmo método que controlava o menu principal.

- **Refactoring aplicado:** Extract Method. O fluxo foi separado em `menuCadastro()`, `menuContratante()` e `menuPrestador()`, cada um com seu próprio laço e sua opção de voltar.

- **Por que essa alteração foi feita?:** Cada menu passa a ter uma responsabilidade clara. Para mudar as opções de um perfil, basta alterar o método correspondente, sem risco de afetar os outros.

- - **Link do PR:** [Commit e941d1d - Refatorações do menu](https://github.com/C14-Inatel-2026-2/Prestador_de_Servico/commit/e941d1d03e7d4f19b353e49f97c7cb3fe6ee5c2b)


---

## 🚀 Tecnologias Utilizadas
* **Linguagem:** Java
* **Banco de Dados:** PostgreSQL / MySQL


---


## 🤖 Uso de Inteligência Artificial (IA)

Durante o desenvolvimento do projeto, foram utilizados modelos de Inteligência Artificial como ferramentas de apoio à implementação, compreensão e resolução de dúvidas técnicas.

A IA foi utilizada para auxiliar na compreensão da estrutura do código, no funcionamento de métodos e comandos, na implementação de funcionalidades, na elaboração de testes e na identificação de possíveis erros. As sugestões fornecidas foram analisadas e adaptadas conforme as necessidades do projeto.

### Ferramentas utilizadas

- **ChatGPT Plus:** utilizado como ferramenta de apoio para esclarecimento de dúvidas técnicas, compreensão da implementação, desenvolvimento de códigos, elaboração de testes e identificação de possíveis melhorias.
- **Claude:** utilizado para tirar dúvidas sobre como funcionava alguns comandos, utilizado na criação de testes, e na criação de alguns trechos de códigos.
- **Gemini:** utilizado para tirar dúvidas , guiar o desenvolvimento de CI/CD funcional entre os membros da equipe , ajuda na criação dos códigos de testes e códigos no geral.

### Histórico de utilização

As conversas abaixo documentam algumas das interações realizadas durante o desenvolvimento do projeto, organizadas de acordo com sua finalidade.

**1. Desenvolvimento e estrutura do código**
- [01 - Desenvolviemnto geral do código do projeto](https://share.gemini.google/VERUe7AkB8R0)
- [01.5 - Desenvolviemnto geral do código do projeto](https://claude.ai/share/314dc797-9ab8-499e-9e3d-93a72ab4c886)
- [02 - Descrição do assunto abordado](LINK_DA_CONVERSA)
- [03 - Descrição do assunto abordado](LINK_DA_CONVERSA)

**2. Compreensão de métodos, comandos e funcionalidades**
- [Explicação de como funcionava o gerenciamento de dependencia](https://claude.ai/chat/cfe0fdcc-251d-45ed-a964-4e20e19fb261)
- [Explicação de como gerenciar o arquivo pom.xml](https://share.gemini.google/VERUe7AkB8R0,)
- [03 - Descrição do assunto abordado](LINK_DA_CONVERSA)

**3. Testes e validações**
- [Criação de testes unitários](https://chatgpt.com/c/6aa36b2c-eac8-83e9-98fd-e1cea1b2e69c)
- [02 - Ajuda com criação dos testes : https://claude.ai/share/b9a843b8-790d-452b-91e8-426140799d19](LINK_DA_CONVERSA)

**4. Correção de erros e resolução de dúvidas**
- [Registro de estudo da Anna: dúvidas sobre o projeto tiradas com o Claude, de 17/10 a 13/11 (PDF)](https://drive.google.com/file/d/1bpaavvzRE4e_4XegEV3Vk19_LpPPy-7V/view?usp=sharing)
- [01 - Dúvidas de como implantar e manter Jenkins atualizado pra equipe ](https://claude.ai/share/b9a843b8-790d-452b-91e8-426140799d19)
- [02 - Descrição do assunto abordado](LINK_DA_CONVERSA)

**5. Apoio na criação do banco de dados**
- [Integração do Banco de dados](https://chatgpt.com/share/6af72606-bd44-83e8-9fe1-f14d09ea765f)
- [Análise na construção do Banco](https://chatgpt.com/share/6af7269a-de4c-83e8-b2f8-6887ba51d795)
- [Criação do menu relacionado ao Banco](https://chatgpt.com/share/6af726de-bebc-83e9-bf4c-fbb17520548d)

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


---

## 🚀 Tecnologias Utilizadas
* **Linguagem:** Java
* **Banco de Dados:** PostgreSQL / MySQL


---

## 🤖 Uso de IA 
 Foram utilizados modelos de IA como:
* Chat GPT Plus - utilizado para fazer códigos e testes:
[1](https://chatgpt.com/share/6af72606-bd44-83e8-9fe1-f14d09ea765f), [2](https://chatgpt.com/share/6af7269a-de4c-83e8-b2f8-6887ba51d795), [3](https://chatgpt.com/share/6af726de-bebc-83e9-bf4c-fbb17520548d), [4](https://chatgpt.com/share/6af726de-bebc-83e9-bf4c-fbb17520548d)


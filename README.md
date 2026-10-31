# Prestador de Serviços

Projeto da disciplina C14 — Engenharia de Software

## Finalidade

Contratar um serviço doméstico hoje depende de indicação de conhecido ou de grupo de WhatsApp. Quem precisa de um encanador não tem como comparar preço e não tem como saber se a pessoa que vai entrar na casa dela é confiável. Do outro lado, o prestador autônomo depende do boca a boca e não tem onde mostrar o próprio histórico de trabalho.

O objetivo do projeto é resolver os dois lados desse problema: dar ao contratante um lugar para encontrar e comparar prestadores e ao prestador um lugar para ser encontrado e construir reputação.

## Ideia principal

Uma aplicação de cadastro e busca de prestadores de serviço de pedreiros, encanadores, eletricistas, montadores de móveis, etc.

O contratante busca pela profissão que precisa e vê os prestadores disponíveis com o valor da hora ou do serviço específico e a nota média de quem já contratou. Compara, escolhe e depois do atendimento deixa uma avaliação com nota de 1 a 5 e comentário. Cada avaliação entra no cálculo da nota média que é o que aparece para o próximo contratante.

O prestador cadastra os serviços que oferece e o valor que cobra, e acompanha as avaliações que recebeu.

A avaliação vale para os dois lados: prestador também avalia o contratante

## Requisitos Funcionais

Os requisitos funcionais descrevem as funcionalidades que o sistema deve disponibilizar aos contratantes e prestadores de serviços.

- *RF01 – Cadastro de Contratantes:* O sistema deve permitir que novos contratantes realizem seu cadastro, informando os dados necessários para a criação de uma conta.

- *RF02 – Cadastro de Prestadores:* O sistema deve permitir que profissionais realizem seu cadastro na plataforma, registrando suas informações.

- *RF03 – Autenticação de Usuários:* O sistema deve permitir que contratantes e prestadores realizem login utilizando suas credenciais cadastradas.

- *RF04 – Alteração de Senha:* O sistema deve permitir que o usuário altere sua senha de acesso, mediante validação de sua senha atual.

- *RF05 – Cadastro de Serviços:* O sistema deve permitir que os prestadores cadastrem os serviços que oferecem, informando os respectivos valores cobrados.

- *RF06 – Pesquisa de Prestadores:* O sistema deve permitir que os contratantes pesquisem prestadores de acordo com a profissão ou categoria de serviço desejada.

- *RF07 – Consulta de Informações dos Prestadores:* O sistema deve apresentar informações dos prestadores cadastrados, incluindo os serviços oferecidos, os valores cobrados e suas respectivas notas médias.

- *RF08 – Avaliação de Prestadores:* O sistema deve permitir que os contratantes avaliem os prestadores após a realização de um serviço, atribuindo uma nota de 1 a 5 e registrando um comentário.

- *RF09 – Atualização da Nota Média:* O sistema deve atualizar a nota média do prestador considerando as avaliações recebidas.

- *RF10 – Consulta de Avaliações Recebidas:* O sistema deve permitir que os prestadores consultem as avaliações realizadas pelos contratantes.

- *RF11 – Avaliação de Contratantes:* O sistema deve permitir que os prestadores registrem avaliações dos contratantes, possibilitando a avaliação de ambas as partes envolvidas na prestação do serviço.

- *RF12 – Histórico de Avaliações do Contratante:* O sistema deve permitir que os contratantes consultem as avaliações que já realizaram para os prestadores.

## Requisitos Não Funcionais

Os requisitos não funcionais descrevem características de qualidade, segurança e desempenho que devem ser consideradas no desenvolvimento e funcionamento da aplicação.

- *RNF01 – Segurança de Senhas:* As senhas dos usuários devem ser armazenadas utilizando mecanismos seguros de hash, evitando o armazenamento de senhas em texto puro.

- *RNF02 – Integridade dos Dados:* O sistema deve preservar a consistência das informações cadastradas, garantindo que as avaliações estejam corretamente associadas aos respectivos contratantes e prestadores.

- *RNF03 – Persistência dos Dados:* As informações cadastradas devem ser preservadas após o encerramento da aplicação, permitindo sua recuperação em execuções posteriores.

- *RNF04 – Usabilidade:* O sistema deve apresentar funcionalidades e mensagens de retorno de maneira clara, facilitando a utilização por contratantes e prestadores.

- *RNF05 – Desempenho:* As operações de pesquisa e consulta de prestadores e serviços devem ser executadas de maneira eficiente, evitando demora desnecessária na apresentação dos resultados.

- *RNF06 – Manutenibilidade:* O código-fonte deve ser organizado e estruturado de maneira que facilite a manutenção, a identificação de problemas e a implementação de novas funcionalidades.

- *RNF07 – Confiabilidade:* O sistema deve tratar situações de erro e entradas inválidas, evitando comportamentos inesperados durante a utilização das funcionalidades.

## Organização e Gerenciamento do Projeto

O desenvolvimento do projeto é organizado utilizando a metodologia ágil Scrum, com o apoio da ferramenta Jira.

O Jira é utilizado para organizar o Product Backlog, cadastrar as histórias de usuário, definir critérios de aceitação, atribuir responsáveis e planejar as atividades das Sprints.

Cada integrante participa da elaboração das histórias de usuário e da organização das atividades, contribuindo para o desenvolvimento colaborativo da aplicação.

*Ferramenta de gerenciamento ágil:* https://projeto-eng-de-software.atlassian.net/jira/software/projects/SCRUM/boards/1?filter=&groupBy=none

## Membros

| Integrante | GitHub |
| --- | --- |
| Anna Beatriz da Silva Bittencourt | @AnnaBittencourt19 |
| Gabriel Faria Monteiro | @GabrielFaria1509 |
| Samuel Marques da Silva | @Samuelmarques7 |
| Layla Victória Sousa Teles | Laylateles |
| Giovanna de Lima Ribeiro | GiovannaLimaRibeiro |

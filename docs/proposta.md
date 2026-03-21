Tenho o desafio abaixo para implementar. Voce deve criar o plano para atendê-lo. Requerido:
- Springboot + maven no backend
- Vuejs + Vite + Tailwindcss no front
- Cinco camandas (front + bff + orquestration layer + microservico + postgres18)

Voce deve utilizar a versao mais recente para todas as tecnologias citadas, sem exceção. Por exemplo: Java 25, Vuejs 3.5.x, Vite 7.x, Tailwindcss 4.x, Springboot 4.0.4 etc.

Para documentacao REST, utilizar springdoc. 
Para documentacoes RMQ utilizar springwolf.
Para documentacao dos fluxos da orquestration layer, criar um sistema de annotation scans que permite ao frontend exibir o fluxo (como se fosse um cockpit do Camunda, mas sem engine - deve ser lightweight).
Para modalegem de tabelas, seguir à risca `modelagem-tabelas.png`

Demais requisitos:
- Suporte integral à i18n + Vite, para os idiomas pt-br e ingles
- Criacao de design system proprio no front (componentes Base*.vue) reutilizaveis, utilizando Tailwind
- Dark theme, com chaveador entre claro e escuro (usando BaseSwitcher)

A arquitetura deve OBRIGATORIAMENTE seguir os padroes:
- DRY
- SOLID
- DDD / TDD
- Clean Code, Clean Architecture
- MVVM, sendo Pinia o responsvel pela camana "ViewModel"

Toda essa stack deve ser implantavel com docker compose ao termino, inclusive com DML de dados. Ao rodar o docker compose, a solucao deve estar 'ready to use'

Tecnologias:
- Frontend: Vue 3 + Vite 7 + Tailwindcss 4 + Pinia + Lucide (icons)
- Backend: Ecossistema Springboot 4

Camadas: 
- front -> bff (agregador/API GW) -> orquestrador (unico com regras de fluxo mas sem regras de negocio) -> microservicos (unico com regras de negocios mas sem regras de fluxo, quase operando como nanoservices)
- Keycloak: IAM, usado apenas pelo frontend e BFF
- Vault: todas as variaveis de ambiente dos projetos Java sem excecao devem estar no Vault
- Rabbitmq: comunicacao sincrona (sendAndReceive) entre Orquestrador e Microservico


Objetivo:
Criar um projeto utilizando as boas práticas de mercado e apresentar o mesmo demonstrando o passo a passo de sua criação (base de dados, tecnologias, aplicação, metodologias, frameworks, etc).

Projeto:
	O projeto consiste em um cadastro de livros. 
No final deste documento foi disponibilizado um modelo dos dados.

Tecnologia:
	A tecnologia a ser utilizada é sempre Web e referente a vaga em que está concorrendo. 
A implementação do projeto ficará por sua total responsabilidade assim como os componentes a serem utilizados (relatórios, camada de persistência, etc) com algumas premissas
	O banco de dados é o de sua preferência. A utilização de camada de persistência também é escolha sua.

Instruções:
Deve ser feito CRUD para Livro, Autor e Assunto conforme o modelo de dados.
A tela inicial pode ter um menu simples ou mesmo links direto para as telas construídas.
O modelo do banco deve ser seguido integralmente, salvo para ajustes de melhoria de performance.
A interface pode ser simples, mas precisa utilizar algum CSS que comande no mínimo a cor e tamanho dos componentes em tela (utilização do bootstrap será um diferencial).
	Os campos que pedem formatação devem possuir o mesmo (data, moeda, etc).
	Deve ser feito obrigatoriamente um relatório (utilizando o componente de relatórios de sua preferência(Crystal, ReportViewer, etc)) e a consulta desse relatório deve ser proveniente de uma view criada no banco de dados. Este relatório pode ser simples, mas permita o entendimento dos dados. O relatório deve trazer as informações das 3 tabelas principais agrupando os dados por autor (atenção pois um livro pode ter mais de autor).
	TDD (Test Driven Development) será considerados um diferencial.
	Tratamento de erros é essencial, evite try catchs genéricos em situações que permitam utilização de tratamentos específicos, como os possíveis erros de banco de dados.
	As mensagens emitidas pelo sistema, labels e etc ficam a seu critério.
	O modelo inicial não prevê, mas é necessário incluir um campo de valor (R$) para o livro.
	Guarde todos os scripts e instruções para implantação de seu projeto, eles devem ser demonstrados na apresentação.
	
Apresentação:
	O teste deve ser apresentado na entrevista técnica que será agendada. A ideia é discutir seu projeto, avaliar o mesmo funcionalmente e tecnicamente.


CRITICO: algumas dicas para se atentar ao fazer o desafio tecnico:

- É a hora de demonstrar que conhece tecnologias como docker, APIs bem escritas e documentadas. 
- Um backlog bem estruturado, com camadas bem definidas, usando frameworks de mercado que tragam alta produtividade e alto desempenho. 
- Mostrar conhecimento em banco de dados usando chave primária, views, tabelas,procedures, etc.
- Tratar as excessões
- Git - merge, ribase, etc
- Lembrar de usar máscara para valor
- Fazer relatório detalhado do banco (procedure, views, triggers);
- Se optar por utilizar framework, use algum que realmente tenha conhecimento, alterar idioma, deixar ajustado;
- API bem escrita;
- Backlog com camadas bem definidas;
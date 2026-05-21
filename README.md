# 🏋️‍♂️ Avalia FIT - Sistema de Gestão Nutricional e Avaliação Física

> **⚠️ Aviso de Status:** Este projeto está atualmente em fase de desenvolvimento contínuo (Work in Progress). A arquitetura do sistema está recebendo atualizações constantes para aprimoramento de performance e segurança.

## 📖 Sobre o Projeto
O **Avalia FIT** é uma aplicação web robusta voltada para a gestão de clínicas de nutrição e acompanhamento de avaliação física. O sistema atua como uma ponte inteligente e integrada entre o profissional de saúde e o paciente.

A aplicação permite desde a marcação simplificada de consultas até o monitoramento detalhado e a evolução corporal do paciente, registrando métricas essenciais como peso, altura, percentual de gordura, massa magra e cálculo automatizado de Índice de Massa Corporal (IMC).

Este projeto está sendo desenvolvido como parte de estudos aprofundados em **Engenharia de Software** e aprimoramento prático em desenvolvimento **Full-Stack**.

## 🚀 Funcionalidades

### ✅ Implementadas (Prontas para Uso)
* **🔐 Autenticação e Autorização:** Login seguro implementado via Spring Security com emissão e validação de tokens JWT (JSON Web Tokens).
* **🛡️ Controle de Acesso (RBAC):** Níveis de permissão estritos e bem definidos para diferentes perfis:
  * `ROLE_PACIENTE`
  * `ROLE_FUNCIONARIO`
  * `ROLE_GERENTE`
  * `ROLE_ADMIN`
* **📅 Gerenciamento de Agenda:** Profissionais de saúde podem disponibilizar, alterar e gerenciar seus horários de atendimento de forma dinâmica.
* **📝 Agendamento de Consultas:** Interface para pacientes visualizarem horários livres da equipe e realizarem a marcação de suas próprias avaliações.
* **📊 Registro de Avaliação Física:** Módulo técnico para inserção de dados biométricos do paciente com cálculo imediato do IMC realizado pelo Back-end.
* **📈 Dashboard do Paciente:** Painel inteligente que exibe os dados da última avaliação física do paciente logado, com proteção de rotas via Módulos JavaScript.
* **⚙️ Arquitetura Front-end Modular:** Consumo de API centralizado com interceptação de tokens e rotas dinâmicas, reduzindo duplicação de código.

### 🚧 Em Desenvolvimento / Planejadas
* **📈 Gráficos de Evolução:** Inclusão de gráficos de linha no Dashboard para mostrar o histórico de progresso do paciente ao longo do tempo.
* **🍏 Plano Alimentar Dinâmico:** Exibição detalhada e personalizada do plano de dieta prescrito pelo nutricionista.
* **🤖 Assistente de IA Integrado:** Chatbot nativo no sistema para fornecer insights personalizados de treinos e orientações de dieta com base nos dados de bioimpedância do usuário.

## 💻 Tecnologias Utilizadas

### Back-end
* **Linguagem:** Java 17
* **Framework Principal:** Spring Boot 3
* **Segurança:** Spring Security & JWT (Json Web Token)
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Gerenciador de Dependências:** Gradle

### Banco de Dados
* **SGBD:** MySQL Server (Porta 3306)

### Front-end
* **Estrutura:** HTML5
* **Estilização:** Tailwind CSS
* **Arquitetura & Consumo de API:** JavaScript Assíncrono (Vanilla JS / ES6 Modules com Fetch API e Client Centralizado)

---

## 🔒 Segurança e Arquitetura de Rede

O sistema foi projetado com práticas modernas de segurança de rede e flexibilidade de infraestrutura:

* **Configuração de Origem Dinâmica:** O Front-end utiliza uma arquitetura de `CONFIG` com `window.location`, permitindo que o sistema identifique automaticamente a origem da requisição (IP ou Domínio). Isso resolve de forma nativa bloqueios de CORS e permite que o sistema rode em qualquer servidor ou rede (VMs, Nuvem, Local) sem necessidade de alterar o código-fonte (Hardcode de IPs).
* **Proteção de Rota Administrativa (IP Whitelisting):** A área e os endpoints de Administração possuem uma camada dupla de segurança. Além de exigir o token JWT com a permissão `ROLE_ADMIN`, o sistema valida o endereço IP da requisição. O acesso é restrito exclusivamente ao endereço de *loopback* (`127.0.0.1` ou `localhost`), garantindo que apenas usuários operando fisicamente a máquina servidora possam gerenciar o sistema.

## 👨‍💻 Autor
Desenvolvido com dedicação por **Tiago de Aquino Nunes**.
* 🎓 Estudante de Engenharia de Software — Universidade Católica de Brasília (UCB)
* 💻 Técnico em Informática
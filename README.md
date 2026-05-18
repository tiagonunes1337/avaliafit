⚠️ **Aviso de Status:** Este projeto está atualmente em fase de desenvolvimento (*Work in Progress*). Algumas funcionalidades ainda estão sendo implementadas, e o sistema pode apresentar instabilidades temporárias devido a refatorações e correções constantes.

---

## 📖 Sobre o Projeto

O **Avalia FIT** é uma aplicação web completa voltada para a gestão de clínicas de nutrição e acompanhamento de avaliação física. O sistema atua como uma ponte inteligente e integrada entre o profissional de saúde e o paciente.

A aplicação permite desde a marcação simplificada de consultas até o monitoramento detalhado e a evolução corporal do paciente, registrando métricas essenciais como peso, altura, percentual de gordura, massa magra e cálculo automatizado de Índice de Massa Corporal (IMC).

> *Este projeto está sendo desenvolvido como parte de estudos aprofundados em Engenharia de Software e aprimoramento prático em desenvolvimento Full-Stack.*

---

## 🚀 Funcionalidades

### **Implementadas (Prontas para Uso)**
* 🔐 **Autenticação e Autorização:** Login seguro e robusto implementado via **Spring Security** com emissão e validação de tokens **JWT (JSON Web Tokens)**.
* 🛡️ **Controle de Acesso (RBAC):** Níveis de permissão estritos e bem definidos para diferentes perfis de usuários:
    * `ROLE_PACIENTE`
    * `ROLE_FUNCIONARIO`
    * `ROLE_GERENTE`
    * `ROLE_ADMIN`
* 📅 **Gerenciamento de Agenda:** Os profissionais de saúde podem disponibilizar, alterar e gerenciar seus horários de atendimento de forma dinâmica.
* 📝 **Agendamento de Consultas:** Interface para pacientes visualizarem horários livres e realizarem a marcação de suas próprias avaliações.
* 📊 **Registro de Avaliação Física:** Módulo técnico para inserção de dados biométricos do paciente com processamento e cálculo imediato do IMC realizado diretamente pelo Back-end.

### **Em Desenvolvimento / Planejadas**
* 🔄 **Dashboard do Paciente:** Painel visual com gráficos de linha mostrando a evolução física histórica e exibição do plano alimentar (Atualmente em fase de correção na integração do fluxo de dados).
* 🤖 **Assistente de IA:** Chatbot integrado nativamente no sistema para fornecer dicas personalizadas de treinos e orientações de dieta com base no perfil do usuário.

---

## 💻 Tecnologias Utilizadas

### **Back-end**
* **Linguagem:** Java 17
* **Framework Principal:** Spring Boot 3
* **Segurança:** Spring Security & JWT (Json Web Token)
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Gerenciador de Dependências:** Maven

### **Banco de Dados**
* **SGBD:** MySQL Server (Porta padrão 3306)

### **Front-end**
* **Estrutura:** HTML5
* **Estilização:** Tailwind CSS (via CDN)
* **Comportamento & Consumo de API:** JavaScript Assíncrono (Vanilla JS com Fetch API)

---

👨‍💻 Autor
Desenvolvido com dedicação por Tiago de Aquino Nunes.

🎓 Estudante de Engenharia de Software — Universidade Católica de Brasília (UCB)

💻 Técnico em Informática

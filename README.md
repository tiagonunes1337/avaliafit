# 🏋️‍♂️ Avalia FIT - Sistema de Gestão Nutricional e Avaliação Física

> **⚠️ Aviso de Status:** Este projeto está atualmente em fase de desenvolvimento contínuo (Work in Progress). A arquitetura do sistema está recebendo atualizações constantes para aprimoramento de performance e segurança.

## 📖 Sobre o Projeto
O **Avalia FIT** é uma aplicação web robusta voltada para a gestão de clínicas de nutrição e acompanhamento de avaliação física. O sistema atua como uma ponte inteligente e integrada entre o profissional de saúde e o paciente.

A aplicação permite desde a marcação simplificada de consultas até o monitoramento detalhado e a evolução corporal do paciente, registrando métricas essenciais como peso, altura, percentual de gordura, massa magra e cálculo automatizado de Índice de Massa Corporal (IMC).

O Avalia Fit foi concebido para proporcionar facilidade ao cliente e ao nutricionista, com uma jornada intuitiva para o paciente e controles administrativos seguros para a equipe clínica.

O produto também está preparado para evolução com integração de Inteligência Artificial, abrindo caminho para análises de progresso e recomendações clínicas sofisticadas.

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

* **Configuração de Origem Dinâmica:** O Front-end utiliza uma arquitetura de `CONFIG` com `window.location`, permitindo que o sistema identifique automaticamente a origem da requisição (IP ou Domínio). Isso ajuda a evitar bloqueios de CORS e permite que o sistema rode em qualquer servidor ou rede (VMs, Nuvem, Local) sem necessidade de alterar o código-fonte.
* **Proteção de Rota Administrativa (IP Whitelisting):** A área e os endpoints de administração possuem uma camada adicional de controle. Além de exigir o token JWT, o sistema restringe o acesso a páginas administrativas a perfis autorizados.

## 🛡️ Segurança e Compliance Clínico

O Avalia Fit foi construído com um foco explícito em proteção de dados clínicos e conformidade regulatória.

* **Identidade e Controle de Acesso (RBAC):** O sistema exige que todo usuário tenha um papel definido. Não há acesso anônimo ou sem papel. Os papéis principais são `ROLE_PACIENTE`, `ROLE_FUNCIONARIO` e `ROLE_ADMIN`.
* **Integridade de Identidade:** O banco de dados bloqueia duplicidade de CPF e e-mail, prevenindo criação de contas falsas e sobreposição de prontuários.
* **Motor de Auditoria:** A tabela `AuditoriaAvaliacao` registra qualquer alteração em dados sensíveis de avaliação. Cada ajuste grava o usuário que alterou, o campo alterado, valor anterior, valor novo, data/hora e motivo da alteração.
* **Compliance LGPD:** O sistema garante responsabilização. Não existem alterações sem autor ou justificativa. Isso é essencial para proteger a clínica contra fraudes e questões legais.

## 🧬 Modelo de Dados Principais

### 1. Tabela `usuario`
Armazena os dados básicos de todos os usuários do sistema:
* `idUsuario` — chave primária.
* `nome` — nome completo.
* `dataNascimento` — data de nascimento.
* `email` — e-mail único.
* `senha` — senha de acesso.
* `cpf` — CPF único.
* `telefone` — telefone de contato.
* `role` — nível de acesso (`ROLE_PACIENTE`, `ROLE_FUNCIONARIO`, `ROLE_ADMIN`).

### 2. Tabela `paciente`
Detalhes específicos dos pacientes:
* `idPaciente` — chave primária referenciando `idUsuario`.
* `objetivo` — objetivo do paciente (ex: ganho de massa, perda de peso).

### 3. Tabela `funcionario`
Detalhes específicos dos funcionários:
* `idFuncionario` — chave primária referenciando `idUsuario`.
* `cargo` — função específica do funcionário (nutricionista, gerente, admin).

### 4. Tabela `avaliacao`
Registra as medições e a bioimpedância:
* `idAvaliacao` — chave primária.
* `idPaciente` — paciente avaliado.
* `idFuncionario` — profissional que realizou a avaliação.
* `dataAvaliacao` — data e hora da avaliação.
* `peso`, `altura`, `imc` — medidas antropométricas.
* `percentualGordura`, `massaMuscular` — resultados da bioimpedância.
* `observacoes` — campo de texto para anotações.

### 5. Tabela `plano_nutricional`
Detalha o plano alimentar:
* `idPlano` — chave primária.
* `idPaciente`, `idFuncionario` — identificam o paciente e o criador do plano.
* `kcalDiario` — ingestão calórica diária.
* `proteinas`, `carboidratos`, `gorduras` — distribuição de macronutrientes.
* `dataInicio` — data de início do plano.
* `ativo` — indica se o plano está ativo.

### 6. Tabela `cardapio_refeicao`
Detalha as refeições dentro de um plano:
* `idRefeicao` — chave primária.
* `idPlano` — plano nutricional ao qual a refeição pertence.
* `nomeRefeicao` — nome da refeição (café da manhã, almoço, etc.).
* `descricao` — detalhes dos alimentos e preparo.

### 7. Tabela `agendamento`
Gerencia as consultas e serviços:
* `idAgendamento` — chave primária.
* `idPaciente`, `idFuncionario` — identificam quem agendou e com quem.
* `dataConsulta`, `horario` — dia e hora agendados.
* `tipoServico` — serviço prestado.
* `status` — situação do agendamento.
* `observacoes` — notas sobre o agendamento.

### 8. Tabela `horario_disponivel`
Controla a agenda dos funcionários:
* `idHorario` — chave primária.
* `idFuncionario` — funcionário associado.
* `data`, `horario` — dia e hora disponíveis.
* `disponivel` — indica se o horário está livre (1) ou ocupado (0).

## 👨‍💻 Autor
Desenvolvido com dedicação por **Tiago de Aquino Nunes**.
* 🎓 Estudante de Engenharia de Software — Universidade Católica de Brasília (UCB)
* 💻 Técnico em Informática

**Configuração Local**
- **Arquivo local:** As configurações sensíveis devem ficar no arquivo local `src/main/resources/application-local.yaml`, que não é versionado.
- **Template:** Use o arquivo de exemplo [src/main/resources/application-template.yml](src/main/resources/application-template.yml) como base. Copie e personalize para o seu ambiente local:

```bash
cp src/main/resources/application-template.yml src/main/resources/application-local.yaml
# editar src/main/resources/application-local.yaml com credenciais locais (NUNCA commitar)
```
- **Env file:** Copie `.env.example` para `.env` e preencha suas credenciais locais. O Spring Boot carregará essas variáveis do ambiente automaticamente.

```bash
cp .env.example .env
source .env
./gradlew bootRun
```
- **Regra:** Não commit e não compartilhe `.env` ou `application-local.yaml`. O repositório já contém uma entrada em `.gitignore` para isso.

**Avaliação do Sistema**
- **Arquitetura:** Backend com Spring Boot 3 (Java 17) e JPA/Hibernate; front-end estático com módulos JavaScript. Estrutura modular e boa separação de camadas.
- **Pontos Fortes:** Autenticação com JWT, RBAC bem definido, modelagem de domínio (usuário/paciente/funcionário/avaliação) coerente, e rotas protegidas.
- **Riscos e Observações de Segurança:**
  - Segredos (senha do BD, `jwt.secret`) não devem estar em arquivos versionados; usar variáveis de ambiente ou um cofre (Vault).
  - Verifique hashing de senhas (usar `BCrypt` ou `Argon2`) e políticas de senha/complexidade.
  - A estratégia de whitelist de IP para área administrativa é uma camada adicional, mas não substitui controles de rede e VPNs; cuidado com deploys em nuvem.
- **Banco de Dados:** O esquema SQL inicial está presente em `bancodedados.sql`. Recomendo adotar migrações automáticas (Flyway ou Liquibase) para versionamento do schema.
- **Testes e CI:** Cobertura de testes unitários e de integração é essencial; configure pipeline (GitHub Actions/Gradle CI) para executar testes, linters e checks de segurança em PRs.
- **Observabilidade:** Adicionar logs estruturados, métricas (Prometheus) e monitoramento de erros (Sentry) facilitará operação e debugging.
- **Próximos passos recomendados:**
  1. Mover segredos para variáveis de ambiente e documentar `.env.example` (sem valores sensíveis).
  2. Adotar migrações (Flyway) e integração contínua.
  3. Revisão de dependências e atualizações regulares (evitar dependências com vulnerabilidades conhecidas).
  4. Implementar testes end-to-end para fluxos críticos (login, agendamento, registro de avaliação).

O site está sendo atualizado e ainda está em desenvolvimento.
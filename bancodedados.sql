CREATE DATABASE IF NOT EXISTS avaliafit;
USE avaliafit;

-- ================================================
-- TABELA BASE
-- ================================================
CREATE TABLE IF NOT EXISTS usuario (
    idUsuario       INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    dataNascimento  DATE NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    senha           VARCHAR(255) NOT NULL,
    cpf             VARCHAR(14)  NOT NULL UNIQUE,
    telefone        VARCHAR(20)  NOT NULL,
    role            VARCHAR(50)  NOT NULL -- ROLE_PACIENTE, ROLE_FUNCIONARIO, ROLE_GERENTE, ROLE_ADMIN
);

-- ================================================
-- ESPECIALIZAÇÕES
-- ================================================
CREATE TABLE IF NOT EXISTS paciente (
    idPaciente  INT PRIMARY KEY,
    objetivo    VARCHAR(255) NOT NULL,
    -- Medições removidas daqui: ficam só em avaliacao
    CONSTRAINT fk_paciente_usuario FOREIGN KEY (idPaciente)
        REFERENCES usuario(idUsuario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS funcionario (
    idFuncionario INT PRIMARY KEY,
    cargo         VARCHAR(100) NOT NULL, -- nutricionista, educador_fisico, gerente, admin
    CONSTRAINT fk_funcionario_usuario FOREIGN KEY (idFuncionario)
        REFERENCES usuario(idUsuario) ON DELETE CASCADE
);

-- ================================================
-- AVALIAÇÃO / BIOIMPEDÂNCIA (histórico completo)
-- ================================================
CREATE TABLE IF NOT EXISTS avaliacao (
    idAvaliacao       INT AUTO_INCREMENT PRIMARY KEY,
    idPaciente        INT            NOT NULL,
    idFuncionario     INT            NOT NULL,
    dataAvaliacao     DATETIME       NOT NULL,
    peso              DECIMAL(5,2)   NOT NULL,
    altura            DECIMAL(5,2)   NOT NULL,
    imc               DECIMAL(5,2)   NOT NULL,
    percentualGordura DECIMAL(5,2)   NOT NULL,
    massaMuscular     DECIMAL(5,2)   NOT NULL,
    observacoes       TEXT,
    CONSTRAINT fk_aval_paciente  FOREIGN KEY (idPaciente)    REFERENCES paciente(idPaciente),
    CONSTRAINT fk_aval_func      FOREIGN KEY (idFuncionario) REFERENCES funcionario(idFuncionario)
);

-- ================================================
-- PLANO NUTRICIONAL
-- ================================================
CREATE TABLE IF NOT EXISTS plano_nutricional (
    idPlano       INT AUTO_INCREMENT PRIMARY KEY,
    idPaciente    INT  NOT NULL,
    idFuncionario INT  NOT NULL,
    kcalDiario    INT  NOT NULL,
    proteinas     INT  NOT NULL,
    carboidratos  INT  NOT NULL,
    gorduras      INT  NOT NULL,
    dataInicio    DATE NOT NULL,
    ativo         TINYINT(1) DEFAULT 1,
    CONSTRAINT fk_plano_paciente FOREIGN KEY (idPaciente)    REFERENCES paciente(idPaciente),
    CONSTRAINT fk_plano_func     FOREIGN KEY (idFuncionario) REFERENCES funcionario(idFuncionario)
);
-- ================================================
-- CARDAPIO REFEIÇÃO
-- ================================================
CREATE TABLE IF NOT EXISTS cardapio_refeicao (
    idRefeicao    INT AUTO_INCREMENT PRIMARY KEY,
    idPlano       INT          NOT NULL,
    nomeRefeicao  VARCHAR(100) NOT NULL, -- café da manhã, almoço, etc
    descricao     TEXT         NOT NULL,
    CONSTRAINT fk_refeicao_plano FOREIGN KEY (idPlano) 
        REFERENCES plano_nutricional(idPlano)
);

-- ================================================
-- AGENDAMENTO (criado completo, sem ALTER)
-- ================================================
CREATE TABLE IF NOT EXISTS agendamento (
    idAgendamento INT AUTO_INCREMENT PRIMARY KEY,
    idPaciente    INT          NOT NULL,
    idFuncionario INT          NOT NULL,
    dataConsulta  DATE         NOT NULL,
    horario       TIME         NOT NULL,
    tipoServico   VARCHAR(100) NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'agendado', -- agendado, cancelado, concluido
    observacoes   TEXT,
    CONSTRAINT fk_agend_paciente FOREIGN KEY (idPaciente)    REFERENCES paciente(idPaciente),
    CONSTRAINT fk_agend_func     FOREIGN KEY (idFuncionario) REFERENCES funcionario(idFuncionario)
);


-- ================================================
-- AUDITORIA_AVALIACAO
-- ================================================
CREATE TABLE IF NOT EXISTS auditoria_avaliacao (
   idAuditoria         INT AUTO_INCREMENT PRIMARY KEY,
    idAvaliacao         INT NOT NULL,
    idFuncionarioAlterou INT NOT NULL,
     campoAlterado       VARCHAR(100) NOT NULL,
    valorAnterior       VARCHAR(255) NOT NULL,
    valorNovo           VARCHAR(255) NOT NULL,
    dataAlteracao       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo              VARCHAR(500),

    CONSTRAINT fk_audit_aval FOREIGN KEY (idAvaliacao)
    REFERENCES avaliacao(idAvaliacao) ON DELETE CASCADE,
    CONSTRAINT fk_audit_func FOREIGN KEY (idFuncionarioAlterou)
    REFERENCES funcionario(idFuncionario) ON DELETE RESTRICT
    );

-- ✅ Índices para performance
CREATE INDEX idx_auditoria_avaliacao ON auditoria_avaliacao(idAvaliacao);
CREATE INDEX idx_auditoria_funcionario ON auditoria_avaliacao(idFuncionarioAlterou);
CREATE INDEX idx_auditoria_data ON auditoria_avaliacao(dataAlteracao);

-- ================================================
-- AGENDAMENTO (criado completo)
-- ================================================
CREATE TABLE IF NOT EXISTS horario_disponivel (
    idHorario     INT AUTO_INCREMENT PRIMARY KEY,
    idFuncionario INT          NOT NULL,
    data          DATE         NOT NULL,
    horario       TIME         NOT NULL,
    disponivel    TINYINT(1)   DEFAULT 1,
    CONSTRAINT fk_horario_func FOREIGN KEY (idFuncionario) 
        REFERENCES funcionario(idFuncionario)
);

-- =====================================================================
-- Educa+ - Modelo físico (MySQL 8) - versão 3 (usada pela API e pelo app)
-- Base: script original do grupo. O que entrou na v2 está marcado com [v2];
-- o que entrou agora para o app funcionar está marcado com [v3].
-- Rodar em um banco novo (o script não apaga um educa_mais já existente).
--
-- Mapa do protótipo para o banco:
--   Ilha    -> disciplina           Região  -> conteudo (ordem dentro do ano)
--   Nível   -> atividade (ordem)    Desafio -> questao
--   Estrelas, loja, personagem, IA tutora -> tabelas novas no fim
-- =====================================================================
CREATE DATABASE IF NOT EXISTS educa_mais CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE educa_mais;

-- Pessoas com acesso à plataforma (alunos, professores e coordenadores).
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    google_sub VARCHAR(64) NULL,                    -- [v2] identificador da conta Google (login)
    perfil ENUM('ALUNO','PROFESSOR','COORDENADOR') NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acesso DATETIME NULL,                    -- [v3] mostrado na lista de professores
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_google_sub UNIQUE (google_sub)
) ENGINE=InnoDB;

-- Turmas de alunos de um mesmo ano escolar.
CREATE TABLE turma (
    id_turma INT AUTO_INCREMENT NOT NULL,
    nome VARCHAR(50) NOT NULL,
    ano_escolar TINYINT NOT NULL,
    ano_letivo SMALLINT NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_turma PRIMARY KEY (id_turma),
    CONSTRAINT ck_turma_ano_escolar CHECK (ano_escolar BETWEEN 1 AND 9)
) ENGINE=InnoDB;

-- Dados específicos do usuário com perfil de aluno.
CREATE TABLE aluno (
    id_usuario INT NOT NULL,
    id_turma INT NOT NULL,
    saldo_estrelas INT NOT NULL DEFAULT 0,          -- [v2] moeda do jogo (atualizada por gatilho)
    cor_pele CHAR(7) NULL,                          -- [v2] personagem, ex.: '#C68B59'
    cor_cabelo CHAR(7) NULL,                        -- [v2]
    CONSTRAINT pk_aluno PRIMARY KEY (id_usuario),
    CONSTRAINT fk_aluno_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_aluno_id_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma) ON DELETE RESTRICT,
    CONSTRAINT ck_aluno_saldo CHECK (saldo_estrelas >= 0)
) ENGINE=InnoDB;

-- Disciplinas oferecidas na plataforma (cada uma é uma ilha no mapa).
CREATE TABLE disciplina (
    id_disciplina INT AUTO_INCREMENT NOT NULL,
    nome VARCHAR(50) NOT NULL,
    codigo VARCHAR(10) NOT NULL,                    -- [v3] código usado pelo app: mat, por, cie, his, geo, ing
    nome_ilha VARCHAR(60) NULL,                     -- [v2] ex.: 'Ilha dos Números'
    cor CHAR(7) NULL,                               -- [v2] cor da ilha na interface
    CONSTRAINT pk_disciplina PRIMARY KEY (id_disciplina),
    CONSTRAINT uk_disciplina_nome UNIQUE (nome),
    CONSTRAINT uk_disciplina_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

-- [v2] Em quais anos escolares a escola oferece cada disciplina (tela Disciplinas do coordenador).
CREATE TABLE disciplina_ano (
    id_disciplina INT NOT NULL,
    ano_escolar TINYINT NOT NULL,
    CONSTRAINT pk_disciplina_ano PRIMARY KEY (id_disciplina, ano_escolar),
    CONSTRAINT fk_disciplina_ano_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE CASCADE,
    CONSTRAINT ck_disciplina_ano_ano CHECK (ano_escolar BETWEEN 1 AND 9)
) ENGINE=InnoDB;

-- Professor que ministra cada disciplina em uma turma (RN012).
CREATE TABLE disciplina_turma (
    id_disciplina_turma INT AUTO_INCREMENT NOT NULL,
    id_turma INT NOT NULL,
    id_disciplina INT NOT NULL,
    id_professor INT NOT NULL,
    data_vinculo DATE NOT NULL,
    CONSTRAINT pk_disciplina_turma PRIMARY KEY (id_disciplina_turma),
    CONSTRAINT uk_disciplina_turma UNIQUE (id_turma, id_disciplina),
    CONSTRAINT fk_disciplina_turma_id_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma) ON DELETE CASCADE,
    CONSTRAINT fk_disciplina_turma_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE RESTRICT,
    CONSTRAINT fk_disciplina_turma_id_professor FOREIGN KEY (id_professor) REFERENCES usuario (id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- [v2] Disciplinas ligadas/desligadas por aluno pelo professor. Sem linha = ligada.
CREATE TABLE aluno_disciplina (
    id_aluno INT NOT NULL,
    id_disciplina INT NOT NULL,
    habilitada BOOLEAN NOT NULL DEFAULT TRUE,
    id_professor_alteracao INT NULL,
    data_alteracao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_aluno_disciplina PRIMARY KEY (id_aluno, id_disciplina),
    CONSTRAINT fk_aluno_disciplina_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_aluno_disciplina_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE CASCADE,
    CONSTRAINT fk_aluno_disciplina_id_professor FOREIGN KEY (id_professor_alteracao) REFERENCES usuario (id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- [v2] Habilidades que a IA acompanha (ex.: subtração, frações, interpretação de texto).
CREATE TABLE habilidade (
    id_habilidade INT AUTO_INCREMENT NOT NULL,
    id_disciplina INT NOT NULL,
    nome VARCHAR(60) NOT NULL,
    CONSTRAINT pk_habilidade PRIMARY KEY (id_habilidade),
    CONSTRAINT uk_habilidade_nome UNIQUE (id_disciplina, nome),
    CONSTRAINT fk_habilidade_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Materiais de estudo por disciplina e ano escolar (UC005). No mapa, cada conteúdo é uma região da ilha.
CREATE TABLE conteudo (
    id_conteudo INT AUTO_INCREMENT NOT NULL,
    id_disciplina INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT NULL,
    material TEXT NOT NULL,
    ano_escolar TINYINT NOT NULL,
    ordem SMALLINT NOT NULL DEFAULT 1,              -- [v2] ordem da região na trilha
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_conteudo PRIMARY KEY (id_conteudo),
    CONSTRAINT fk_conteudo_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE RESTRICT,
    CONSTRAINT ck_conteudo_ano_escolar CHECK (ano_escolar BETWEEN 1 AND 9)
) ENGINE=InnoDB;

-- Conjuntos de questões sobre um conteúdo. No mapa, cada atividade é um nível da trilha.
CREATE TABLE atividade (
    id_atividade INT AUTO_INCREMENT NOT NULL,
    id_conteudo INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT NULL,
    ordem SMALLINT NOT NULL DEFAULT 1,              -- [v2] posição do nível dentro da região
    dificuldade TINYINT NOT NULL DEFAULT 1,         -- [v2] 1 fácil, 2 médio, 3 difícil
    tipo_minijogo VARCHAR(20) NULL,                 -- [v3] motor do jogo (ex.: subtracao, fracao)
    id_habilidade INT NULL,                         -- [v3] habilidade principal do nível
    id_autor INT NULL,                              -- [v2] professor que criou (NULL = trilha padrão)
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_atividade PRIMARY KEY (id_atividade),
    CONSTRAINT fk_atividade_id_conteudo FOREIGN KEY (id_conteudo) REFERENCES conteudo (id_conteudo) ON DELETE RESTRICT,
    CONSTRAINT fk_atividade_id_autor FOREIGN KEY (id_autor) REFERENCES usuario (id_usuario) ON DELETE SET NULL,
    CONSTRAINT fk_atividade_id_habilidade FOREIGN KEY (id_habilidade) REFERENCES habilidade (id_habilidade) ON DELETE SET NULL,
    CONSTRAINT ck_atividade_dificuldade CHECK (dificuldade BETWEEN 1 AND 3)
) ENGINE=InnoDB;
CREATE INDEX ix_atividade_trilha ON atividade (id_conteudo, ordem);

-- Questões que compõem uma atividade (cada uma é um desafio do minijogo).
CREATE TABLE questao (
    id_questao INT AUTO_INCREMENT NOT NULL,
    id_atividade INT NOT NULL,
    enunciado TEXT NOT NULL,
    ordem SMALLINT NOT NULL,
    tipo ENUM('MULTIPLA_ESCOLHA','VERDADEIRO_FALSO','FORMAR_PALAVRA','CLASSIFICAR','ORDENAR')
        NOT NULL DEFAULT 'MULTIPLA_ESCOLHA',        -- [v2] tipo de minijogo
    dica VARCHAR(300) NULL,                         -- [v2] pista do tutor, sem dar a resposta
    midia VARCHAR(255) NULL,                        -- [v2] imagem ou emoji do desafio
    id_habilidade INT NULL,                         -- [v2] o que a IA usa para achar dificuldades
    id_autor INT NULL,                              -- [v2] professor que criou a questão
    situacao ENUM('EM_REVISAO','PUBLICADA','RECUSADA') NOT NULL DEFAULT 'PUBLICADA', -- [v2] revisão da coordenação
    CONSTRAINT pk_questao PRIMARY KEY (id_questao),
    CONSTRAINT uk_questao_ordem UNIQUE (id_atividade, ordem),
    CONSTRAINT fk_questao_id_atividade FOREIGN KEY (id_atividade) REFERENCES atividade (id_atividade) ON DELETE CASCADE,
    CONSTRAINT fk_questao_id_habilidade FOREIGN KEY (id_habilidade) REFERENCES habilidade (id_habilidade) ON DELETE SET NULL,
    CONSTRAINT fk_questao_id_autor FOREIGN KEY (id_autor) REFERENCES usuario (id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Opções de resposta de uma questão.
CREATE TABLE alternativa (
    id_alternativa INT AUTO_INCREMENT NOT NULL,
    id_questao INT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    correta BOOLEAN NOT NULL,
    grupo VARCHAR(50) NULL,                         -- [v2] CLASSIFICAR: em qual grupo o item vai (ex.: 'Água')
    posicao SMALLINT NULL,                          -- [v2] ORDENAR: posição certa na sequência
    CONSTRAINT pk_alternativa PRIMARY KEY (id_alternativa),
    CONSTRAINT fk_alternativa_id_questao FOREIGN KEY (id_questao) REFERENCES questao (id_questao) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Atividades atribuídas às turmas, com prazo (UC004).
CREATE TABLE atribuicao_atividade (
    id_atribuicao INT AUTO_INCREMENT NOT NULL,
    id_atividade INT NOT NULL,
    id_turma INT NOT NULL,
    data_atribuicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prazo_entrega DATETIME NOT NULL,
    situacao ENUM('EM_ANDAMENTO','ENCERRADA','CANCELADA') NOT NULL DEFAULT 'EM_ANDAMENTO',
    CONSTRAINT pk_atribuicao_atividade PRIMARY KEY (id_atribuicao),
    CONSTRAINT uk_atribuicao_turma UNIQUE (id_atividade, id_turma),
    CONSTRAINT fk_atribuicao_atividade_id_atividade FOREIGN KEY (id_atividade) REFERENCES atividade (id_atividade) ON DELETE RESTRICT,
    CONSTRAINT fk_atribuicao_atividade_id_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma) ON DELETE RESTRICT,
    CONSTRAINT ck_atribuicao_prazo CHECK (prazo_entrega >= data_atribuicao)
) ENGINE=InnoDB;

-- Cada resolução de uma atividade por um aluno (UC003).
CREATE TABLE realizacao_atividade (
    id_realizacao INT AUTO_INCREMENT NOT NULL,
    id_aluno INT NOT NULL,
    id_atividade INT NOT NULL,
    id_atribuicao INT NULL,
    modo ENUM('TRILHA','REFORCO','ATRIBUIDA') NOT NULL DEFAULT 'TRILHA', -- [v2] reforço = treino criado pela IA
    data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_envio DATETIME NULL,
    situacao ENUM('EM_ANDAMENTO','ENVIADA') NOT NULL DEFAULT 'EM_ANDAMENTO',
    percentual_acertos DECIMAL(5,2) NULL,
    acertos SMALLINT NOT NULL DEFAULT 0,            -- [v3] desafios acertados de primeira
    estrelas TINYINT NULL,                          -- [v2] 1 a 3 ao concluir
    erros SMALLINT NOT NULL DEFAULT 0,              -- [v2]
    dicas_usadas SMALLINT NOT NULL DEFAULT 0,       -- [v2]
    CONSTRAINT pk_realizacao_atividade PRIMARY KEY (id_realizacao),
    CONSTRAINT fk_realizacao_atividade_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_realizacao_atividade_id_atividade FOREIGN KEY (id_atividade) REFERENCES atividade (id_atividade) ON DELETE RESTRICT,
    CONSTRAINT fk_realizacao_atividade_id_atribuicao FOREIGN KEY (id_atribuicao) REFERENCES atribuicao_atividade (id_atribuicao) ON DELETE SET NULL,
    CONSTRAINT ck_realizacao_percentual CHECK (percentual_acertos BETWEEN 0 AND 100),
    CONSTRAINT ck_realizacao_estrelas CHECK (estrelas BETWEEN 0 AND 3)
) ENGINE=InnoDB;

-- Respostas do aluno às questões em uma realização (fica a resposta final de cada questão).
CREATE TABLE resposta (
    id_resposta INT AUTO_INCREMENT NOT NULL,
    id_realizacao INT NOT NULL,
    id_questao INT NOT NULL,
    id_alternativa INT NULL,
    correta BOOLEAN NOT NULL,
    tentativas TINYINT NOT NULL DEFAULT 1,          -- [v2] quantas vezes tentou até acertar
    tempo_resposta_seg SMALLINT NULL,               -- [v2] a IA usa o tempo de resposta
    usou_dica BOOLEAN NOT NULL DEFAULT FALSE,       -- [v2]
    data_resposta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_resposta PRIMARY KEY (id_resposta),
    CONSTRAINT uk_resposta_questao UNIQUE (id_realizacao, id_questao),
    CONSTRAINT fk_resposta_id_realizacao FOREIGN KEY (id_realizacao) REFERENCES realizacao_atividade (id_realizacao) ON DELETE CASCADE,
    CONSTRAINT fk_resposta_id_questao FOREIGN KEY (id_questao) REFERENCES questao (id_questao) ON DELETE RESTRICT,
    CONSTRAINT fk_resposta_id_alternativa FOREIGN KEY (id_alternativa) REFERENCES alternativa (id_alternativa) ON DELETE RESTRICT,
    CONSTRAINT ck_resposta_tentativas CHECK (tentativas >= 1)
) ENGINE=InnoDB;

-- Histórico de desempenho do aluno por disciplina e mês (RN007).
CREATE TABLE desempenho_disciplina (
    id_desempenho INT AUTO_INCREMENT NOT NULL,
    id_aluno INT NOT NULL,
    id_disciplina INT NOT NULL,
    mes_referencia TINYINT NOT NULL,
    ano_referencia SMALLINT NOT NULL,
    atividades_concluidas INT NOT NULL DEFAULT 0,
    percentual_acertos DECIMAL(5,2) NOT NULL,
    evolucao DECIMAL(6,2) NULL,
    CONSTRAINT pk_desempenho_disciplina PRIMARY KEY (id_desempenho),
    CONSTRAINT uk_desempenho_periodo UNIQUE (id_aluno, id_disciplina, mes_referencia, ano_referencia),
    CONSTRAINT fk_desempenho_disciplina_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_desempenho_disciplina_id_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina (id_disciplina) ON DELETE RESTRICT,
    CONSTRAINT ck_desempenho_mes CHECK (mes_referencia BETWEEN 1 AND 12)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- [v2] Gamificação: loja, personagem, estrelas e conquistas
-- ---------------------------------------------------------------------

-- Itens cosméticos que o aluno compra com estrelas (sem dinheiro real).
CREATE TABLE item_loja (
    id_item INT AUTO_INCREMENT NOT NULL,
    categoria ENUM('CABELO','ROUPA','ACESSORIO','MOCHILA','PET') NOT NULL,
    codigo VARCHAR(30) NOT NULL,                    -- usado pelo app para desenhar o item
    nome VARCHAR(60) NOT NULL,
    custo_estrelas INT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_item_loja PRIMARY KEY (id_item),
    CONSTRAINT uk_item_loja_codigo UNIQUE (categoria, codigo),
    CONSTRAINT ck_item_loja_custo CHECK (custo_estrelas >= 0)
) ENGINE=InnoDB;

-- Itens que cada aluno possui e quais está usando no personagem.
CREATE TABLE aluno_item (
    id_aluno INT NOT NULL,
    id_item INT NOT NULL,
    data_aquisicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    equipado BOOLEAN NOT NULL DEFAULT FALSE,        -- regra do app: 1 item equipado por categoria
    CONSTRAINT pk_aluno_item PRIMARY KEY (id_aluno, id_item),
    CONSTRAINT fk_aluno_item_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_aluno_item_id_item FOREIGN KEY (id_item) REFERENCES item_loja (id_item) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Extrato de estrelas: tudo que entra (níveis, bônus) e sai (compras). O saldo fica em aluno.
CREATE TABLE movimento_estrelas (
    id_movimento INT AUTO_INCREMENT NOT NULL,
    id_aluno INT NOT NULL,
    tipo ENUM('GANHO','COMPRA') NOT NULL,
    quantidade INT NOT NULL,
    id_realizacao INT NULL,
    id_item INT NULL,
    descricao VARCHAR(150) NULL,
    data_movimento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_movimento_estrelas PRIMARY KEY (id_movimento),
    CONSTRAINT fk_movimento_estrelas_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_movimento_estrelas_id_realizacao FOREIGN KEY (id_realizacao) REFERENCES realizacao_atividade (id_realizacao) ON DELETE SET NULL,
    CONSTRAINT fk_movimento_estrelas_id_item FOREIGN KEY (id_item) REFERENCES item_loja (id_item) ON DELETE SET NULL,
    CONSTRAINT ck_movimento_quantidade CHECK (quantidade > 0)
) ENGINE=InnoDB;

-- Medalhas da tela Meu Progresso.
CREATE TABLE conquista (
    id_conquista INT AUTO_INCREMENT NOT NULL,
    nome VARCHAR(60) NOT NULL,
    descricao VARCHAR(150) NOT NULL,
    CONSTRAINT pk_conquista PRIMARY KEY (id_conquista),
    CONSTRAINT uk_conquista_nome UNIQUE (nome)
) ENGINE=InnoDB;

CREATE TABLE aluno_conquista (
    id_aluno INT NOT NULL,
    id_conquista INT NOT NULL,
    data_conquista DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_aluno_conquista PRIMARY KEY (id_aluno, id_conquista),
    CONSTRAINT fk_aluno_conquista_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_aluno_conquista_id_conquista FOREIGN KEY (id_conquista) REFERENCES conquista (id_conquista) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- [v2] IA educacional (feita pelo próprio grupo, parte do sistema)
-- ---------------------------------------------------------------------

-- Dificuldades que a IA detectou em um aluno (ex.: errou 2 vezes seguidas a mesma habilidade).
CREATE TABLE dificuldade_detectada (
    id_dificuldade INT AUTO_INCREMENT NOT NULL,
    id_aluno INT NOT NULL,
    id_habilidade INT NOT NULL,
    qtd_erros SMALLINT NOT NULL DEFAULT 1,
    data_deteccao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolvida BOOLEAN NOT NULL DEFAULT FALSE,       -- vira TRUE quando o aluno passa no reforço
    data_resolucao DATETIME NULL,
    CONSTRAINT pk_dificuldade_detectada PRIMARY KEY (id_dificuldade),
    CONSTRAINT fk_dificuldade_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_dificuldade_id_habilidade FOREIGN KEY (id_habilidade) REFERENCES habilidade (id_habilidade) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX ix_dificuldade_aberta ON dificuldade_detectada (id_aluno, resolvida);

-- Insights gerados para professores (por turma) e para a coordenação (escola, id_turma NULL).
CREATE TABLE insight_ia (
    id_insight INT AUTO_INCREMENT NOT NULL,
    publico ENUM('PROFESSOR','COORDENADOR') NOT NULL,
    id_turma INT NULL,
    id_aluno INT NULL,                              -- [v3] insight sobre um aluno específico
    id_habilidade INT NULL,
    tipo ENUM('DIFICULDADE','AVANCO','ATENCAO','RECOMENDACAO') NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    texto TEXT NOT NULL,
    situacao ENUM('NOVO','APLICADO','DISPENSADO') NOT NULL DEFAULT 'NOVO',
    data_geracao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_insight_ia PRIMARY KEY (id_insight),
    CONSTRAINT fk_insight_ia_id_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma) ON DELETE CASCADE,
    CONSTRAINT fk_insight_ia_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_insight_ia_id_habilidade FOREIGN KEY (id_habilidade) REFERENCES habilidade (id_habilidade) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Conversa do aluno com o tutor (Tico).
CREATE TABLE mensagem_tutor (
    id_mensagem INT AUTO_INCREMENT NOT NULL,
    id_aluno INT NOT NULL,
    autor ENUM('ALUNO','TUTOR') NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    id_questao INT NULL,                            -- questão em que o aluno estava, se houver
    data_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_mensagem_tutor PRIMARY KEY (id_mensagem),
    CONSTRAINT fk_mensagem_tutor_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_tutor_id_questao FOREIGN KEY (id_questao) REFERENCES questao (id_questao) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- [v2] Regras no banco: saldo de estrelas e compra na loja
-- ---------------------------------------------------------------------
DELIMITER $$

-- Mantém aluno.saldo_estrelas igual à soma do extrato. Se a compra deixar o saldo
-- negativo, o CHECK ck_aluno_saldo falha e o movimento inteiro é desfeito.
CREATE TRIGGER trg_movimento_saldo AFTER INSERT ON movimento_estrelas
FOR EACH ROW
BEGIN
    UPDATE aluno
       SET saldo_estrelas = saldo_estrelas + IF(NEW.tipo = 'GANHO', NEW.quantidade, -NEW.quantidade)
     WHERE id_usuario = NEW.id_aluno;
END$$

-- Ao concluir um nível, credita 10 estrelas para cada estrela conquistada.
CREATE TRIGGER trg_realizacao_credito_ins AFTER INSERT ON realizacao_atividade
FOR EACH ROW
BEGIN
    IF NEW.situacao = 'ENVIADA' AND NEW.estrelas > 0 THEN
        INSERT INTO movimento_estrelas (id_aluno, tipo, quantidade, id_realizacao, descricao)
        VALUES (NEW.id_aluno, 'GANHO', NEW.estrelas * 10, NEW.id_realizacao, 'Nível concluído');
    END IF;
END$$

CREATE TRIGGER trg_realizacao_credito_upd AFTER UPDATE ON realizacao_atividade
FOR EACH ROW
BEGIN
    IF OLD.situacao <> 'ENVIADA' AND NEW.situacao = 'ENVIADA' AND NEW.estrelas > 0 THEN
        INSERT INTO movimento_estrelas (id_aluno, tipo, quantidade, id_realizacao, descricao)
        VALUES (NEW.id_aluno, 'GANHO', NEW.estrelas * 10, NEW.id_realizacao, 'Nível concluído');
    END IF;
END$$

-- Compra um item: confere se existe, se o aluno já tem e se há estrelas; tudo numa transação.
CREATE PROCEDURE sp_comprar_item(IN p_aluno INT, IN p_item INT)
BEGIN
    DECLARE v_custo INT DEFAULT NULL;
    DECLARE v_saldo INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT custo_estrelas INTO v_custo FROM item_loja WHERE id_item = p_item AND ativo = TRUE;
    IF v_custo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Item não encontrado na loja';
    END IF;
    IF EXISTS (SELECT 1 FROM aluno_item WHERE id_aluno = p_aluno AND id_item = p_item) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O aluno já tem este item';
    END IF;
    SELECT saldo_estrelas INTO v_saldo FROM aluno WHERE id_usuario = p_aluno FOR UPDATE;
    IF v_saldo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Aluno não encontrado';
    END IF;
    IF v_saldo < v_custo THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Estrelas insuficientes';
    END IF;

    INSERT INTO aluno_item (id_aluno, id_item) VALUES (p_aluno, p_item);
    IF v_custo > 0 THEN
        INSERT INTO movimento_estrelas (id_aluno, tipo, quantidade, id_item, descricao)
        VALUES (p_aluno, 'COMPRA', v_custo, p_item, 'Compra na loja');
    END IF;
    COMMIT;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
-- [v2] Visões que alimentam as telas
-- ---------------------------------------------------------------------

-- Trilha de cada aluno: nível, melhor nota em estrelas e se já está liberado.
-- Só entram disciplinas oferecidas no ano da turma e ligadas para o aluno.
CREATE VIEW vw_trilha_aluno AS
WITH melhor AS (
    SELECT id_aluno, id_atividade, MAX(estrelas) AS estrelas
      FROM realizacao_atividade
     WHERE situacao = 'ENVIADA' AND modo = 'TRILHA'
     GROUP BY id_aluno, id_atividade
), trilha AS (
    SELECT al.id_usuario AS id_aluno,
           d.id_disciplina, d.nome AS disciplina, d.nome_ilha,
           c.id_conteudo, c.titulo AS regiao,
           atv.id_atividade, atv.titulo AS titulo_nivel, atv.dificuldade,
           ROW_NUMBER() OVER (PARTITION BY al.id_usuario, d.id_disciplina ORDER BY c.ordem, atv.ordem, atv.id_atividade) AS nivel,
           COALESCE(m.estrelas, 0) AS estrelas
      FROM aluno al
      JOIN turma t            ON t.id_turma = al.id_turma
      JOIN conteudo c         ON c.ano_escolar = t.ano_escolar
      JOIN disciplina d       ON d.id_disciplina = c.id_disciplina
      JOIN disciplina_ano da  ON da.id_disciplina = d.id_disciplina AND da.ano_escolar = t.ano_escolar
      JOIN atividade atv      ON atv.id_conteudo = c.id_conteudo
      LEFT JOIN aluno_disciplina ad ON ad.id_aluno = al.id_usuario AND ad.id_disciplina = d.id_disciplina
      LEFT JOIN melhor m      ON m.id_aluno = al.id_usuario AND m.id_atividade = atv.id_atividade
     WHERE COALESCE(ad.habilitada, TRUE) = TRUE
)
SELECT trilha.*,
       (nivel = 1 OR estrelas > 0
        OR LAG(estrelas) OVER (PARTITION BY id_aluno, id_disciplina ORDER BY nivel) > 0) AS liberado
  FROM trilha;

-- Placa de cada ilha no mapa: "7/10 níveis" e estrelas.
CREATE VIEW vw_progresso_ilha AS
SELECT id_aluno, id_disciplina, disciplina, nome_ilha,
       COUNT(*) AS niveis_total,
       SUM(estrelas > 0) AS niveis_concluidos,
       SUM(estrelas) AS estrelas,
       ROUND(100 * SUM(estrelas > 0) / COUNT(*)) AS percentual
  FROM vw_trilha_aluno
 GROUP BY id_aluno, id_disciplina, disciplina, nome_ilha;

-- "12 alunos apresentam dificuldade em multiplicação" (insights do professor).
CREATE VIEW vw_dificuldade_turma AS
SELECT t.id_turma, t.nome AS turma, h.id_habilidade, h.nome AS habilidade,
       COUNT(DISTINCT dd.id_aluno) AS alunos
  FROM dificuldade_detectada dd
  JOIN aluno al     ON al.id_usuario = dd.id_aluno
  JOIN turma t      ON t.id_turma = al.id_turma
  JOIN habilidade h ON h.id_habilidade = dd.id_habilidade
 WHERE dd.resolvida = FALSE
 GROUP BY t.id_turma, t.nome, h.id_habilidade, h.nome;

-- =====================================================================
-- Dados iniciais
-- =====================================================================

-- Disciplinas da plataforma ([v2] + História e Geografia, que o protótipo usa)
INSERT INTO disciplina (id_disciplina, nome, codigo, nome_ilha, cor) VALUES
 (1,'Matemática','mat','Ilha dos Números','#FF7A1A'),
 (2,'Português','por','Ilha das Letras','#8B5CF6'),
 (3,'Inglês','ing','Ilha Hello!','#EC4899'),
 (4,'Ciências','cie','Ilha das Descobertas','#1FB866'),
 (5,'História','his','Ilha do Tempo','#E0533D'),
 (6,'Geografia','geo','Ilha dos Mapas','#2D7FF9');

-- Inglês só a partir do 3º ano; as demais do 1º ao 5º.
INSERT INTO disciplina_ano (id_disciplina, ano_escolar)
SELECT d.id_disciplina, a.ano
  FROM disciplina d
  JOIN (SELECT 1 AS ano UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) a
 WHERE d.codigo <> 'ing' OR a.ano >= 3;

-- Habilidades que a IA acompanha (a API cria novas sozinha se o jogo mandar uma que não existe).
INSERT INTO habilidade (id_disciplina, nome) VALUES
 (1,'contagem'),(1,'adição'),(1,'comparação'),(1,'formas'),(1,'sequências'),(1,'problemas'),
 (1,'adição com dezenas'),(1,'subtração'),(1,'multiplicação'),(1,'frações'),
 (2,'vogais'),(2,'sílabas'),(2,'formar palavras'),(2,'completar frases'),(2,'rimas'),(2,'plural'),
 (2,'leitura de palavras'),(2,'interpretação de texto'),(2,'ortografia'),
 (3,'greetings'),(3,'colors'),(3,'animals'),(3,'numbers'),(3,'fruits'),(3,'vocabulário'),
 (4,'seres vivos'),(4,'habitats'),(4,'plantas'),(4,'corpo humano'),(4,'estados da água'),(4,'experiências'),(4,'insetos'),(4,'reciclagem'),
 (5,'sequência de fatos'),(5,'linha do tempo'),(5,'antigamente'),(5,'povos indígenas'),(5,'datas importantes'),(5,'fontes históricas'),
 (6,'ambientes'),(6,'pontos cardeais'),(6,'mapas'),(6,'capitais');

-- Loja (os itens de custo 0 são os iniciais de todo aluno).
INSERT INTO item_loja (categoria, codigo, nome, custo_estrelas) VALUES
 ('CABELO','curto','Curtinho',0),('CABELO','cacheado','Cacheado',0),('CABELO','longo','Longo',0),('CABELO','coque','Coque',40),('CABELO','moicano','Moicano',60),
 ('ROUPA','laranja','Camiseta laranja',0),('ROUPA','azul','Camiseta azul',0),('ROUPA','listrada','Listrada',50),('ROUPA','macacao','Macacão',70),('ROUPA','heroi','Capa de herói',120),
 ('ACESSORIO','redondos','Óculos redondos',30),('ACESSORIO','sol','Óculos de sol',45),('ACESSORIO','bone','Boné',40),('ACESSORIO','mago','Chapéu de mago',100),('ACESSORIO','coroa','Coroa',150),
 ('MOCHILA','mochila','Mochila azul',35),('MOCHILA','foguete','Mochila foguete',110),
 ('PET','gato','Gatinho',80),('PET','cao','Cachorrinho',80),('PET','dragao','Dragãozinho',200);

INSERT INTO conquista (nome, descricao) VALUES
 ('Explorador','Concluir 10 níveis'),('Mestre dos números','7 níveis de Matemática'),
 ('Leitor curioso','5 níveis de Português'),('Estrela tripla','15 níveis com 3 estrelas'),
 ('Persistente','Terminar um nível depois de errar'),('Estiloso','Comprar 3 itens na loja');

-- Schema do banco do projeto (PostgreSQL), montado a partir dos DAOs.
-- Uso local: rodar uma vez em um banco vazio.

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario   SERIAL PRIMARY KEY,
    nome         VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    senha_hash   VARCHAR(255) NOT NULL,
    telefone     VARCHAR(30),
    cidade       VARCHAR(255),
    tipo         VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS prestadores (
    id_usuario   INTEGER PRIMARY KEY REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    profissao    VARCHAR(255),
    descricao    TEXT,
    valor_hora   NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS contratantes (
    id_usuario   INTEGER PRIMARY KEY REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS servicos (
    id              SERIAL PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    descricao       TEXT,
    preco_sugerido  NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS prestador_servicos (
    id_prestador   INTEGER NOT NULL REFERENCES prestadores(id_usuario) ON DELETE CASCADE,
    id_servico     INTEGER NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    preco_cobrado  NUMERIC(10,2),
    PRIMARY KEY (id_prestador, id_servico)
);

CREATE TABLE IF NOT EXISTS avaliacoes (
    id_avaliacao     SERIAL PRIMARY KEY,
    id_avaliador     INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    id_avaliado      INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    nota             INTEGER NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario       TEXT,
    data_avaliacao   DATE NOT NULL
);

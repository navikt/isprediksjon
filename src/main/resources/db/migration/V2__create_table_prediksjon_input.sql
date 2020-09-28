CREATE TABLE prediksjon_input (
    id                          SERIAL          PRIMARY KEY,
    uuid                        VARCHAR(50)     NOT NULL    UNIQUE,
    fnr                         CHAR(11),
    aktorid                     CHAR(13)        NOT NULL,
    tilfelle_start_date         DATE            NOT NULL,
    tilfelle_end_date           DATE            NOT NULL,
    created                     TIMESTAMP       NOT NULL
);

CREATE TABLE prediksjon_output
(
    id                  SERIAL PRIMARY KEY,
    fnr                 CHAR(11),
    aktorid             CHAR(13)  NOT NULL,
    tilfelle_start_date date      NOT NULL,
    tilfelle_end_date   date      NOT NULL,
    input_created       TIMESTAMP NOT NULL,
    prediksjon_created  TIMESTAMP NOT NULL,
    datastate           text      NOT NULL,
    metadata            jsonb,
    prediksjon_delta    float4
);

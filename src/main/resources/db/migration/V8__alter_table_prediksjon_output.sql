ALTER TABLE prediksjon_output
    ADD COLUMN input_id             integer NOT NULL,
    ADD COLUMN forklaring_raw       jsonb,
    ADD COLUMN forklaring_front_end jsonb;

CREATE INDEX IX_PREDIKSJON_OUTPUT_FNR ON prediksjon_output (fnr);

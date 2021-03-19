ALTER TABLE prediksjon_output
    ADD COLUMN input_id             integer,
    ADD COLUMN forklaring_raw       jsonb,
    ADD COLUMN forklaring_front_end jsonb,
    ALTER COLUMN prediksjon_created SET DEFAULT now();

CREATE INDEX IX_PREDIKSJON_OUTPUT_FNR ON prediksjon_output (fnr);

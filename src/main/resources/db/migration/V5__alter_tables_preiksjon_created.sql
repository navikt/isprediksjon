ALTER TABLE prediksjon_input
ALTER input_created TYPE timestamptz USING input_created AT TIME ZONE 'UTC';

ALTER TABLE prediksjon_output
ALTER input_created TYPE timestamptz USING input_created AT TIME ZONE 'UTC';

ALTER TABLE prediksjon_output
ALTER prediksjon_created TYPE timestamptz USING prediksjon_created AT TIME ZONE 'UTC';

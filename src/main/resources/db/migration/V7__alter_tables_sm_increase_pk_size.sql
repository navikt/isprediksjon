ALTER TABLE smManuellBehandling
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smAutomatiskBehandling
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smHist
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smBehandlingsutfall
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smBehandlingsutfallHist
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smSykmeldingstatus
ALTER COLUMN uniqueId TYPE CHAR(128);

ALTER TABLE smSykmeldingstatusHist
ALTER COLUMN uniqueId TYPE CHAR(128);

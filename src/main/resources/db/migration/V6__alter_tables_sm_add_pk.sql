ALTER TABLE smManuellBehandling
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smAutomatiskBehandling
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smHist
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smBehandlingsutfall
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smBehandlingsutfallHist
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smSykmeldingstatus
ADD uniqueId CHAR(64) PRIMARY KEY;

ALTER TABLE smSykmeldingstatusHist
ADD uniqueId CHAR(64) PRIMARY KEY;



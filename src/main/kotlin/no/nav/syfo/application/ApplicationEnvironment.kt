package no.nav.syfo.application

data class Environment(
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "isprediksjon"),
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),

    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),

    val kafkaBootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS_URL"),
    val oppfolgingstilfelleTopic: String = "aapen-syfo-oppfolgingstilfelle-v1",
    val sm2013ManuellBehandlingTopic: String = getEnvVar("KAFKA_SM2013_MANUELL_BEHANDLING", "privat-syfo-sm2013-manuellBehandling"),
    val sm2013AutomatiskBehandlingTopic: String = getEnvVar("KAFKA_SM2013_AUTOMATISK_BEHANDLING_TOPIC", "privat-syfo-sm2013-automatiskBehandling"),
    val smregisterRecievedSykmeldingBackupTopic: String = getEnvVar("KAFKA_SMREGISTER_RECIEVED_SYKMELDING_BACKUP_TOPIC", "privat-syfosmregister-received-sykmelding-backup"),
    val sm2013BehandlingsutfallTopic: String = getEnvVar("KAFKA_SM2013_BEHANDLINGSUTFALL_TOPIC", "privat-syfo-sm2013-behandlingsUtfall"),
    val smregisterBehandlingsutfallBackupTopic: String = getEnvVar("KAFKA_SMREGISTER_BEHANDLINGSUTFALL_BACKUP_TOPIC", "privat-syfosmregister-behandlingsutfall-backup"),
    val syfoSykmeldingstatusLeesahTopic: String = getEnvVar("KAFKA_SYFO_SYKMELDINGSTATUS_LEESAH_TOPIC", "aapen-syfo-sykmeldingstatus-leesah-v1"),
    val syfoRegisterStatusBackupTopic: String = getEnvVar("KAFKA_SYFO_REGISTER_STATUS_BACKUP_TOPIC", "privat-syfo-register-status-backup"),
    val kafkaConsumerTopics: List<String> = listOf(sm2013ManuellBehandlingTopic, sm2013AutomatiskBehandlingTopic, smregisterRecievedSykmeldingBackupTopic, sm2013BehandlingsutfallTopic, smregisterBehandlingsutfallBackupTopic, syfoSykmeldingstatusLeesahTopic, syfoRegisterStatusBackupTopic),

    val databaseMountPathVault: String = getEnvVar("DATABASE_MOUNT_PATH_VAULT"),
    val databaseName: String = getEnvVar("DATABASE_NAME", "isprediksjon"),
    val isprediksjonDBURL: String = getEnvVar("ISPREDIKSJON_DB_URL"),
    val aktorregisterV1Url: String = getEnvVar("AKTORREGISTER_V1_URL"),
    val stsRestUrl: String = getEnvVar("SECURITY_TOKEN_SERVICE_REST_URL"),
    val syketilfelleUrl: String = getEnvVar("SYFOSYKETILFELLE_URL"),

    val isProcessOppfolgingstilfelleOn: Boolean = getEnvVar("IS_PROCESS_OPPFOLGINGSTILFELLE_ON", "false").toBoolean(),

    val developmentMode: Boolean = getEnvVar("DEVELOPMENT_MODE", "false").toBoolean(),

    val tilgangskontrollUrl: String = getEnvVar("TILGANGSKONTROLL_URL"),
    val syfotilgangskontrollClientId: String = getEnvVar("SYFOTILGANGSKONTROLL_CLIENT_ID"),

    val tilgangPath: String = getEnvVar("TILGANG_PATH", "/var/run/secrets/nais.io/vault/tilgang.json")
)

data class VaultSecrets(
    val serviceuserUsername: String,
    val serviceuserPassword: String
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

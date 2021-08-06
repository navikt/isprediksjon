package no.nav.syfo.application.database

import io.ktor.application.*
import no.nav.syfo.application.Environment
import no.nav.syfo.application.ApplicationState

lateinit var database: DatabaseInterface
fun Application.databaseModule(
    applicationState: ApplicationState,
    env: Environment,
    vaultCredentialService: VaultCredentialService
) {
    log.info("Initialization of database starting")

    val newCredentials = vaultCredentialService.getNewCredentials(
        env.databaseMountPathVault,
        env.databaseName,
        Role.USER
    )

    database = ProdDatabase(
        DbConfig(
            jdbcUrl = env.isprediksjonDBURL,
            username = newCredentials.username,
            password = newCredentials.password,
            databaseName = env.databaseName,
            runMigrationsOninit = false
        )
    ) { prodDatabase ->

        // i prod må vi kjøre flyway migrations med et eget sett brukernavn/passord
        vaultCredentialService.getNewCredentials(env.databaseMountPathVault, env.databaseName, Role.ADMIN).let {
            prodDatabase.runFlywayMigrations(env.isprediksjonDBURL, it.username, it.password)
        }

        vaultCredentialService.renewCredentialsTaskData =
            RenewCredentialsTaskData(env.databaseMountPathVault, env.databaseName, Role.USER) {
                prodDatabase.updateCredentials(username = it.username, password = it.password)
            }

        applicationState.ready = true
    }
    log.info("Initialization of database done")
}

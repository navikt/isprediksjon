package no.nav.syfo

import io.ktor.application.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.database.*

lateinit var database: DatabaseInterface
fun Application.databaseModule(
    applicationState: ApplicationState,
    env: Environment,
    vaultCredentialService: VaultCredentialService
) {
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
}

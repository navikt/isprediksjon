package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.VaultSecrets
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.clients.azuread.v2.wellknown.getWellKnown
import no.nav.syfo.application.database.VaultCredentialService
import no.nav.syfo.application.database.database
import no.nav.syfo.application.database.databaseModule
import no.nav.syfo.kafka.kafkaModule
import no.nav.syfo.util.getFileAsString
import no.nav.syfo.vault.RenewVaultService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.isprediksjon")

val backgroundTasksContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher() + MDCContext()

@InternalAPI
@KtorExperimentalAPI
fun main() {
    val server = embeddedServer(
        Netty,
        applicationEngineEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
            config = HoconApplicationConfig(ConfigFactory.load())

            val env = Environment()

            connector {
                port = env.applicationPort
            }

            val vaultSecrets = VaultSecrets(
                serviceuserPassword = getFileAsString("/secrets/serviceuser/password"),
                serviceuserUsername = getFileAsString("/secrets/serviceuser/username")
            )

            val applicationState = ApplicationState()

            val vaultCredentialService = VaultCredentialService()

            applicationState.alive = true

            if (!env.developmentMode) {
                RenewVaultService(vaultCredentialService, applicationState).startRenewTasks()
            }

            val wellKnownInternADV2 = getWellKnown(env.azureAppWellKnownUrl)

            module {
                databaseModule(
                    applicationState,
                    env,
                    vaultCredentialService
                )
                apiModule(
                    applicationState,
                    database,
                    env,
                    wellKnownInternADV2,
                )
                kafkaModule(
                    applicationState,
                    env,
                    vaultSecrets
                )
            }

            log.info("Hello from isprediksjon")
        }
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )

    server.start(wait = false)
}

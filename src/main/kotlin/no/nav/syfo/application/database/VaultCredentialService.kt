package no.nav.syfo.application.database

import com.bettercloud.vault.VaultException
import kotlinx.coroutines.delay
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.log
import no.nav.syfo.vault.Vault

class VaultCredentialService {
    var leaseDuration: Long = 1
    var renewCredentialsTaskData: RenewCredentialsTaskData? = null

    suspend fun runRenewCredentialsTask(applicationState: ApplicationState) {
        delay(leaseDuration * 1000)
        while (applicationState.alive) {
            renewCredentialsTaskData?.run {
                val credentials = getNewCredentials(
                    mountPath,
                    databaseName,
                    role
                )
                cb(credentials)
            }
            delay(Vault.suggestedRefreshIntervalInMillis(leaseDuration * 1000))
        }
    }

    fun getNewCredentials(mountPath: String, databaseName: String, role: Role): VaultCredentials {
        val path = "$mountPath/creds/$databaseName-$role"
        log.info("Getting database credentials for path '$path'")
        try {
            val response = Vault.client.logical().read(path)
            val username = checkNotNull(response.data["username"]) { "Username is not set in response from Vault" }
            val password = checkNotNull(response.data["password"]) { "Password is not set in response from Vault" }
            log.info("Got new credentials (username=$username, role=${role.name}, leaseDuration=${response.leaseDuration})")
            leaseDuration = response.leaseDuration
            return VaultCredentials(response.leaseId, username, password)
        } catch (e: VaultException) {
            when (e.httpStatusCode) {
                403 -> log.error("Vault denied permission to fetch database credentials for path '$path'", e)
                else -> log.error("Could not fetch database credentials for path '$path'", e)
            }
            throw e
        }
    }
}

data class RenewCredentialsTaskData(
    val mountPath: String,
    val databaseName: String,
    val role: Role,
    val cb: (credentials: VaultCredentials) -> Unit
)

data class VaultCredentials(
    val leaseId: String,
    val username: String,
    val password: String
)

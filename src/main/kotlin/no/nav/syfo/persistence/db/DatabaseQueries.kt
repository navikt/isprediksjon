package no.nav.syfo.persistence.db

import no.nav.syfo.Environment
import no.nav.syfo.util.toPGObject
import java.sql.Connection
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun getSMRegQuery(tablename: String): String {
    return """
    INSERT INTO $tablename(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """
}

fun getSMRegTableNameFromTopic(env: Environment, topic: String): String {
    return when (topic) {
        env.sm2013ManuellBehandlingTopic -> "smManuellBehandling"
        env.sm2013AutomatiskBehandlingTopic -> "smAutomatiskBehandling"
        env.smregisterRecievedSykmeldingBackupTopic -> "smHist"
        env.sm2013BehandlingsutfallTopic -> "smBehandlingsutfall"
        env.smregisterBehandlingsutfallBackupTopic -> "smBehandlingsutfallHist"
        env.syfoSykmeldingstatusLeesahTopic -> "smSykmeldingstatus"
        env.syfoRegisterStatusBackupTopic -> "smSykmeldingstatusHist"
        else -> throw Exception("Unexpected topic: $topic")
    }
}

fun Connection.createSmRegRow(env: Environment, topic: String, uniqueId: String, data: String, sykmeldingId: String) {
    prepareStatement(getSMRegQuery(getSMRegTableNameFromTopic(env, topic))).use {
        it.setString(1, uniqueId)
        it.setString(2, sykmeldingId)
        it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
        it.setObject(4, data.toPGObject())
        it.executeUpdate()
    }
}

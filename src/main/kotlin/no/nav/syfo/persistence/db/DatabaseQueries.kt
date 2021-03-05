package no.nav.syfo.persistence.db

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.util.toPGObject
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val queryCreateSmManuellBehandling =
    """
    INSERT INTO smManuellBehandling(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmManuellBehandling(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmManuellBehandling).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmAutomatiskBehandling =
    """
    INSERT INTO smAutomatiskBehandling(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmAutomatiskBehandling(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmAutomatiskBehandling).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmHist =
    """
    INSERT INTO smHist(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmHist(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmHist).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmBehandlingsutfall =
    """
    INSERT INTO smBehandlingsutfall(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmBehandlingsutfall(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmBehandlingsutfall).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmBehandlingsutfallHist =
    """
    INSERT INTO smBehandlingsutfallHist(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmBehandlingsutfallHist(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmBehandlingsutfallHist).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmSykmeldingstatus =
    """
    INSERT INTO smSykmeldingstatus(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmSykmeldingstatus(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmSykmeldingstatus).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmSykmeldingstatusHist =
    """
    INSERT INTO smSykmeldingstatusHist(
        uniqueId,
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?, ?)
    ON CONFLICT DO NOTHING
    """

fun DatabaseInterface.createSmSykmeldingstatusHist(uniqueId: String, data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmSykmeldingstatusHist).use {
            it.setString(1, uniqueId)
            it.setString(2, sykmeldingId)
            it.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(4, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

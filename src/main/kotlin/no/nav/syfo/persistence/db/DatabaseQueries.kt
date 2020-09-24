package no.nav.syfo.persistence.db

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.util.toPGObject
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val queryCreateSmManuellBehandling =
    """
    INSERT INTO smManuellBehandling(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmManuellBehandling(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmManuellBehandling).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmAutomatiskBehandling =
    """
    INSERT INTO smAutomatiskBehandling(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmAutomatiskBehandling(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmAutomatiskBehandling).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmHist =
    """
    INSERT INTO smHist(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmHist(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmHist).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmBehandlingsutfall =
    """
    INSERT INTO smBehandlingsutfall(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmBehandlingsutfall(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmBehandlingsutfall).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmBehandlingsutfallHist =
    """
    INSERT INTO smBehandlingsutfallHist(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmBehandlingsutfallHist(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmBehandlingsutfallHist).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmSykmeldingstatus =
    """
    INSERT INTO smSykmeldingstatus(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmSykmeldingstatus(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmSykmeldingstatus).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

const val queryCreateSmSykmeldingstatusHist =
    """
    INSERT INTO smSykmeldingstatusHist(
        sykmelding_id,
        created,
        data
        )
    VALUES  (?, ?, ?)
    """

fun DatabaseInterface.createSmSykmeldingstatusHist(data: String, sykmeldingId: String) {
    connection.use { connection ->
        connection.prepareStatement(queryCreateSmSykmeldingstatusHist).use {
            it.setString(1, sykmeldingId)
            it.setTimestamp(2, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))
            it.setObject(3, data.toPGObject())
            it.executeUpdate()
        }
        connection.commit()
    }
}

package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Level
import java.time.LocalDate

data class Certification(
    val number: String,
    val issuedOn: LocalDate,
    val expiresOn: LocalDate,
    val level: Level
) {
    init {
        require(number.isNotBlank())
        require(!expiresOn.isBefore(issuedOn))
    }
    fun validOn(date: LocalDate) = !date.isBefore(issuedOn) && !date.isAfter(expiresOn)
}

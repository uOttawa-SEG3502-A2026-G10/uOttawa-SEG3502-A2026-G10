package seg3x02.carhms.domain.hikersorganizers

import java.time.LocalDate

data class Waiver(val acceptedOn: LocalDate, val guardianName: String?) {
    init { require(guardianName == null || guardianName.isNotBlank()) }
}

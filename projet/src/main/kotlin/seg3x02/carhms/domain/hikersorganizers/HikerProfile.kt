package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Level
import java.time.LocalDate

data class HikerProfile(
    val names: String,
    val phone: String,
    val address: String,
    val birthDate: LocalDate,
    val emergencyContact: EmergencyContact,
    val skill: Level
) {
    init { require(names.isNotBlank() && phone.isNotBlank() && address.isNotBlank()) }
}

package seg3x02.carhms.domain.registrations

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

class Registration(
    val id: String,
    val hikeId: HikeId,
    val hikerEmail: Email,
    val registeredAt: Instant,
    status: RegistrationStatus
) {
    var status = status
        private set
    init { require(id.isNotBlank()) }
    fun cancel() {
        require(status != RegistrationStatus.CANCELLED) { "Registration already cancelled" }
        status = RegistrationStatus.CANCELLED
    }
    fun promote() {
        require(status == RegistrationStatus.WAITLISTED)
        status = RegistrationStatus.CONFIRMED
    }
}

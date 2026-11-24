package seg3x02.carhms.domain.registrations

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

interface RegistrationFactory {
    fun create(hikeId: HikeId, hiker: Email, at: Instant, status: RegistrationStatus): Registration
}

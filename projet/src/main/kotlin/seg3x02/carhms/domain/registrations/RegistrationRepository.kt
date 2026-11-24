package seg3x02.carhms.domain.registrations

import seg3x02.carhms.domain.HikeId

interface RegistrationRepository {
    fun forHike(hikeId: HikeId): List<Registration>
    fun save(registration: Registration)
}

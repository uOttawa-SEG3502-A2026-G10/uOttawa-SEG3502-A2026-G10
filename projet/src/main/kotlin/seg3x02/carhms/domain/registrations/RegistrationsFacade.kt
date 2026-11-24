package seg3x02.carhms.domain.registrations

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

class RegistrationsFacade(
    private val repository: RegistrationRepository,
    private val factory: RegistrationFactory
) {
    fun all(hikeId: HikeId) = repository.forHike(hikeId)
    fun confirmed(hikeId: HikeId) = all(hikeId).filter { it.status == RegistrationStatus.CONFIRMED }
    fun waitlisted(hikeId: HikeId) = all(hikeId)
        .filter { it.status == RegistrationStatus.WAITLISTED }.sortedBy { it.registeredAt }

    fun signUp(hikeId: HikeId, email: Email, capacity: Int, at: Instant): Registration {
        require(all(hikeId).none { it.hikerEmail == email && it.status != RegistrationStatus.CANCELLED }) {
            "Duplicate registration"
        }
        val status = if (confirmed(hikeId).size < capacity) RegistrationStatus.CONFIRMED else RegistrationStatus.WAITLISTED
        return factory.create(hikeId, email, at, status).also(repository::save)
    }

    fun position(registration: Registration) = waitlisted(registration.hikeId).indexOfFirst { it.id == registration.id } + 1

    fun cancel(hikeId: HikeId, email: Email): CancellationResult {
        val registration = requireNotNull(all(hikeId).find {
            it.hikerEmail == email && it.status != RegistrationStatus.CANCELLED
        }) { "No active registration" }
        val wasConfirmed = registration.status == RegistrationStatus.CONFIRMED
        registration.cancel()
        repository.save(registration)
        val promoted = if (wasConfirmed) waitlisted(hikeId).firstOrNull() else null
        promoted?.let { it.promote(); repository.save(it) }
        return CancellationResult(registration, promoted)
    }

    fun cancelAll(hikeId: HikeId): List<Registration> {
        val active = all(hikeId).filter { it.status != RegistrationStatus.CANCELLED }
        active.forEach { it.cancel(); repository.save(it) }
        return active
    }
}

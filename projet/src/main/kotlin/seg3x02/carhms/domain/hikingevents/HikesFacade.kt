package seg3x02.carhms.domain.hikingevents

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

class HikesFacade(private val repository: HikeRepository, private val factory: HikeFactory) {
    fun get(id: HikeId) = requireNotNull(repository.find(id)) { "Unknown hike" }
    fun all() = repository.all()
    fun create(organizer: Email, details: HikeDetails, at: Instant): Hike {
        require(details.startsAt > at && details.registrationDeadline > at) { "Hike and deadline must be in the future" }
        return factory.create(organizer, details, at).also(repository::save)
    }
    fun save(hike: Hike) = repository.save(hike)
}

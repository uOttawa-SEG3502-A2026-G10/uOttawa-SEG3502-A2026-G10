package seg3x02.carhms.domain.hikingevents

import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE
import java.time.Instant
import java.time.LocalDate

class Hike(
    val id: HikeId,
    val organizerEmail: Email,
    val details: HikeDetails,
    createdAt: Instant
) {
    private val entries = mutableListOf(LifecycleEntry(HikeStatus.OPEN, organizerEmail.value, createdAt))
    val history: List<LifecycleEntry> get() = entries.toList()
    val status: HikeStatus get() = entries.last().status
    val date: LocalDate get() = details.startsAt.atZone(CLUB_ZONE).toLocalDate()
    fun isFinished(at: Instant) = status != HikeStatus.CANCELLED && date.isBefore(at.atZone(CLUB_ZONE).toLocalDate())

    fun requireRegistrationAllowed(at: Instant) {
        require(status == HikeStatus.OPEN || status == HikeStatus.COMPLETE) { "Hike is not accepting registrations" }
        require(at < details.registrationDeadline && at < details.startsAt) { "Registration deadline reached" }
    }

    fun updateCapacity(confirmedCount: Int, actor: String, at: Instant): Boolean {
        requireRegistrationAllowed(at)
        require(confirmedCount in 0..details.capacity)
        return transition(if (confirmedCount == details.capacity) HikeStatus.COMPLETE else HikeStatus.OPEN, actor, at)
    }

    fun close(at: Instant): Boolean {
        if (status == HikeStatus.CANCELLED || status == HikeStatus.CLOSED) return false
        require(at >= details.registrationDeadline) { "Registration deadline not reached" }
        return transition(HikeStatus.CLOSED, "SYSTEM", at)
    }

    fun cancel(actor: String, reason: String, at: Instant): Boolean {
        require(reason.isNotBlank()) { "Cancellation reason required" }
        require(at < details.startsAt) { "Hike already started" }
        if (status == HikeStatus.CANCELLED) return false
        return transition(HikeStatus.CANCELLED, actor, at, reason)
    }

    private fun transition(status: HikeStatus, actor: String, at: Instant, reason: String? = null): Boolean {
        if (this.status == status) return false
        entries.add(LifecycleEntry(status, actor, at, reason))
        return true
    }
}

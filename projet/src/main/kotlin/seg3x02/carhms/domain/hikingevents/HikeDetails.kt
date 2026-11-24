package seg3x02.carhms.domain.hikingevents

import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE
import java.time.Instant

data class HikeDetails(
    val title: String,
    val description: String,
    val trailId: String,
    val startsAt: Instant,
    val durationMinutes: Int,
    val registrationDeadline: Instant,
    val capacity: Int
) {
    init {
        require(title.isNotBlank() && description.isNotBlank() && trailId.isNotBlank())
        require(durationMinutes > 0 && capacity > 0)
        require(registrationDeadline < startsAt) { "Deadline must precede departure" }
        val departureDate = startsAt.atZone(CLUB_ZONE).toLocalDate()
        require(!registrationDeadline.atZone(CLUB_ZONE).toLocalDate().isBefore(departureDate.minusDays(14))) {
            "Deadline is more than two weeks before hike"
        }
    }
}

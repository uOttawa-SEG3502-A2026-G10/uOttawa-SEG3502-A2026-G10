package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.CreateHike
import seg3x02.carhms.domain.hikingevents.*
import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE

class CreateHikeImpl(private val env: UseCaseHelpers) : CreateHike {
    override fun execute(query: CreateHikeQuery): HikeIdDto = env.transactions.run {
        val email = Email.of(query.organizerEmail)
        val principal = requireNotNull(env.users.current()) { "Authentication required" }
        require(principal.email == email) { "Organizer identity mismatch" }
        val date = env.localDate(query.date)
        val time = query.startTime.toInstant().atZone(CLUB_ZONE).toLocalTime()
        val startsAt = date.atTime(time).atZone(CLUB_ZONE).toInstant()
        val trail = env.trails.get(query.trailId)
        trail.requireAvailable(date)
        env.hikers.get(email).requireOrganizer(date, trail.difficulty)
        val details = HikeDetails(query.title, query.description, trail.id, startsAt,
            query.expectedDurationMinutes, query.registrationDeadline.toInstant(), query.capacity)
        val hike = env.hikes.create(email, details, env.clock.instant())
        env.event("HikeCreated", hike.id.value)
        env.lifecycle(hike)
        HikeIdDto(hike.id.value)
    }
}

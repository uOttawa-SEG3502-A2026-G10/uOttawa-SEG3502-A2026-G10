package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.BrowseHikes
import seg3x02.carhms.domain.hikingevents.*
import seg3x02.carhms.domain.*

class BrowseHikesImpl(private val env: UseCaseHelpers) : BrowseHikes {
    override fun execute(query: BrowseHikesQuery): List<HikeSummaryDto> = view(query).hikes

    fun view(query: BrowseHikesQuery): HikeBrowseView = env.transactions.run {
        val difficulty = query.difficulty?.let(Level::parse)
        val date = query.date?.let(env::localDate)
        var results = env.hikes.all().filter {
            it.status == HikeStatus.OPEN && env.clock.instant() < it.details.registrationDeadline && !it.isFinished(env.clock.instant())
        }.mapNotNull { hike ->
            val trail = env.trails.get(hike.details.trailId)
            if (query.trailName != null && !trail.name.contains(query.trailName, ignoreCase = true)) return@mapNotNull null
            if (difficulty != null && trail.difficulty != difficulty) return@mapNotNull null
            if (date != null && hike.date != date) return@mapNotNull null
            HikeSummaryDto(hike.id.value, hike.details.title, trail.name, hike.date, trail.difficulty.difficultyName(),
                env.hikers.get(hike.organizerEmail).profile.names,
                hike.details.capacity - env.registrations.confirmed(hike.id).size)
        }
        if (query.sortByDate) results = results.sortedBy { it.date }
        val principal = env.users.current()
        val skill = principal?.email?.takeIf { seg3x02.carhms.domain.authentication.Role.HIKER in principal.roles }
            ?.let { env.hikers.get(it).profile.skill }
        HikeBrowseView(results, results.filter { skill != null && Level.parse(it.difficulty) == skill }.map { it.id }.toSet())
    }
}

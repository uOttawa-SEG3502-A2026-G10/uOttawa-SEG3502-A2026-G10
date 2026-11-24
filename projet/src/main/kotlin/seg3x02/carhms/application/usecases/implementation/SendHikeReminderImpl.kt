package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.SendHikeReminderQuery
import seg3x02.carhms.application.usecases.SendHikeReminder
import seg3x02.carhms.domain.hikingevents.HikeStatus
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE

class SendHikeReminderImpl(private val env: UseCaseHelpers) : SendHikeReminder {
    override fun execute(query: SendHikeReminderQuery) {
        env.authorization.requireSystem(env.users.current())
        val targetDate = env.localDate(query.targetDate)
        require(targetDate == env.today().plusDays(1)) { "Reminders target tomorrow" }
        val targets = env.transactions.run {
            env.hikes.all().filter { it.date == targetDate && it.status != HikeStatus.CANCELLED }
                .map { hike -> Triple(hike, env.trails.get(hike.details.trailId), env.registrations.confirmed(hike.id).toList()) }
        }
        targets.forEach { (hike, trail, registrations) ->
            val forecast = try { env.weather.forecast(trail.departurePoint, targetDate) } catch (_: Exception) { null }
            val warning = if (forecast?.rain == true || forecast?.snow == true) " Avertissement météo : pluie ou neige prévue." else ""
            val body = "Rappel : ${hike.date}, ${hike.details.startsAt.atZone(CLUB_ZONE).toLocalTime()}, " +
                "${trail.name}, point de départ : ${trail.departurePoint}.$warning"
            registrations.forEach { env.notify(hike, it.hikerEmail, "reminder", body) }
        }
    }
}

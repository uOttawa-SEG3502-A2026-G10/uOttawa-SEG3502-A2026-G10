package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.services.*
import seg3x02.carhms.domain.authentication.AuthorizationFacade
import seg3x02.carhms.domain.hikersorganizers.HikersFacade
import seg3x02.carhms.domain.hikingevents.Hike
import seg3x02.carhms.domain.hikingevents.HikesFacade
import seg3x02.carhms.domain.notifications.*
import seg3x02.carhms.domain.registrations.RegistrationsFacade
import seg3x02.carhms.domain.*
import seg3x02.carhms.domain.ClubTime.Companion.CLUB_ZONE
import seg3x02.carhms.domain.trails.TrailsFacade
import java.time.Clock
import java.time.LocalDate
import java.util.Date

class UseCaseHelpers(
    val hikers: HikersFacade,
    val hikes: HikesFacade,
    val registrations: RegistrationsFacade,
    val trails: TrailsFacade,
    val notifications: NotificationsFacade,
    val users: UserContext,
    val passwords: PasswordHasher,
    val emails: EmailService,
    val weather: WeatherService,
    val events: EventEmitter,
    val transactions: TransactionRunner,
    val clock: Clock
) {
    val authorization = AuthorizationFacade()
    fun today(): LocalDate = clock.instant().atZone(CLUB_ZONE).toLocalDate()
    fun localDate(date: Date): LocalDate = date.toInstant().atZone(CLUB_ZONE).toLocalDate()
    fun actor() = users.current()?.email?.value ?: "SYSTEM"
    fun event(type: String, id: String, details: Map<String, String> = emptyMap(), actor: String = actor()) {
        events.emit(DomainEvent(type, id, actor, clock.instant(), details))
    }
    fun lifecycle(hike: Hike) {
        val entry = hike.history.last()
        events.emit(DomainEvent("HikeStatusChanged", hike.id.value, entry.actor, entry.at,
            mapOf("status" to entry.status.name, "reason" to (entry.reason ?: ""))))
    }

    fun notify(hike: Hike, recipient: Email, kind: String, body: String) {
        val notification = transactions.run {
            val existing = notifications.find(hike.id, recipient, kind)
            if (kind == "reminder" && existing?.status == DeliveryStatus.SENT) return@run null
            if (kind == "reminder" && existing != null) existing else {
                notifications.create(hike.id, recipient, kind, "Cairn: ${hike.details.title}", body, clock.instant())
                    .also { event("NotificationRequested", it.id, mapOf("kind" to kind)) }
            }
        } ?: return
        val failure = try {
            emails.send(notification)
            null
        } catch (_: Exception) {
            "Email transport unavailable"
        }
        transactions.run {
            if (failure == null) notification.sent() else notification.failed(failure)
            notifications.save(notification)
            event(if (failure == null) "NotificationSent" else "NotificationFailed", notification.id)
        }
    }
}

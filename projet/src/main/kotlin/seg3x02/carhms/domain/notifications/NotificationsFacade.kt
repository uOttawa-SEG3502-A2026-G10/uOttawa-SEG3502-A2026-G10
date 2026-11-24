package seg3x02.carhms.domain.notifications

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

class NotificationsFacade(private val repository: NotificationRepository, private val factory: NotificationFactory) {
    fun create(hikeId: HikeId, recipient: Email, kind: String, subject: String, body: String, at: Instant) =
        factory.create(hikeId, recipient, kind, subject, body, at).also(repository::save)
    fun find(hikeId: HikeId, recipient: Email, kind: String) = repository.find(hikeId, recipient, kind)
    fun save(notification: Notification) = repository.save(notification)
}

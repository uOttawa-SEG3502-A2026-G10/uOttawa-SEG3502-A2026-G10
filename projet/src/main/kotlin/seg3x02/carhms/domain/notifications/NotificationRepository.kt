package seg3x02.carhms.domain.notifications

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId

interface NotificationRepository {
    fun save(notification: Notification)
    fun find(hikeId: HikeId, recipient: Email, kind: String): Notification?
}

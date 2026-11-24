package seg3x02.carhms.domain.notifications

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

interface NotificationFactory {
    fun create(hikeId: HikeId, recipient: Email, kind: String, subject: String, body: String, at: Instant): Notification
}

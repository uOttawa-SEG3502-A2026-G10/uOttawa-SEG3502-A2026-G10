package seg3x02.carhms.domain.notifications

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.HikeId
import java.time.Instant

class Notification(
    val id: String,
    val hikeId: HikeId,
    val recipient: Email,
    val kind: String,
    val subject: String,
    val body: String,
    val createdAt: Instant
) {
    var status = DeliveryStatus.PENDING
        private set
    var failure: String? = null
        private set
    fun sent() { status = DeliveryStatus.SENT; failure = null }
    fun failed(message: String) { status = DeliveryStatus.FAILED; failure = message }
}

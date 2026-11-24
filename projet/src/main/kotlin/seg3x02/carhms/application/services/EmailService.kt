package seg3x02.carhms.application.services

import seg3x02.carhms.domain.notifications.Notification

interface EmailService {
    fun send(notification: Notification)
}

package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.SendHikeReminderQuery

interface SendHikeReminder {
    fun execute(query: SendHikeReminderQuery): Unit
}

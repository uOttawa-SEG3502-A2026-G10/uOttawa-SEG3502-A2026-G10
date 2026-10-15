package seg3x02.carhms.application.dtos.queries

import java.util.*

data class CreateHikeQuery(
    val organizerEmail: String,
    val title: String,
    val description: String,
    val trailId: String,
    val date: Date,
    val startTime: Date,
    val expectedDurationMinutes: Int,
    val registrationDeadline: Date,
    val capacity: Int
)

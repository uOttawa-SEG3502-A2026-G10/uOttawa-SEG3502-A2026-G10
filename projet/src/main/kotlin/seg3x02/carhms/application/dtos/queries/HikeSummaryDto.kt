package seg3x02.carhms.application.dtos.queries

import java.time.LocalDate

data class HikeSummaryDto(
    val id: String,
    val title: String,
    val trailName: String,
    val date: LocalDate,
    val difficulty: String,
    val organizerName: String,
    val remainingPlaces: Int
)

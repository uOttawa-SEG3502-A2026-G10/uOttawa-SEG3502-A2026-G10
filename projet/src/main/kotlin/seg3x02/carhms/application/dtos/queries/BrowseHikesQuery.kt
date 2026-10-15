package seg3x02.carhms.application.dtos.queries

import java.util.*

data class BrowseHikesQuery(
    val trailName: String? = null,
    val date: Date? = null,
    val difficulty: String? = null,
    val sortByDate: Boolean = true
)

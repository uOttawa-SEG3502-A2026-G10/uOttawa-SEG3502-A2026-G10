package seg3x02.carhms.domain.trails

import seg3x02.carhms.domain.Level
import java.time.LocalDate

data class Trail(
    val id: String,
    val name: String,
    val difficulty: Level,
    val season: OpeningSeason,
    val hikingPermitted: Boolean,
    val departurePoint: String,
    val type: String = "",
    val surface: String = "",
    val sector: String = "",
    val zone: String = "",
    val parkingAvailable: Boolean = false,
    val geometry: String = ""
) {
    init { require(id.isNotBlank() && name.isNotBlank() && departurePoint.isNotBlank()) }
    fun requireAvailable(date: LocalDate) {
        require(hikingPermitted) { "Hiking is not permitted" }
        require(season.includes(date)) { "Trail is outside its opening season" }
    }
}

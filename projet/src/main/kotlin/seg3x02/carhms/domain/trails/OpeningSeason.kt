package seg3x02.carhms.domain.trails

import java.time.LocalDate
import java.time.MonthDay

data class OpeningSeason(val start: MonthDay? = null, val end: MonthDay? = null) {
    init { require((start == null) == (end == null)) }
    fun includes(date: LocalDate): Boolean {
        if (start == null || end == null) return true
        val day = MonthDay.from(date)
        return if (start <= end) day >= start && day <= end else day >= start || day <= end
    }
}

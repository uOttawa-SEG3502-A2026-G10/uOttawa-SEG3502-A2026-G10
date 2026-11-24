package seg3x02.carhms.domain.trailreports

import java.time.LocalDate

interface TrailReportsFacade {
    fun usage(trailId: String, from: LocalDate, to: LocalDate): TrailUsage
}

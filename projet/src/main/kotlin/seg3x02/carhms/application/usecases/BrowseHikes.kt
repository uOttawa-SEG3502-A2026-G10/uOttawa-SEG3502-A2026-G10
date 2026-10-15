package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.BrowseHikesQuery
import seg3x02.carhms.application.dtos.queries.HikeSummaryDto

interface BrowseHikes {
    fun execute(query: BrowseHikesQuery): List<HikeSummaryDto>
}

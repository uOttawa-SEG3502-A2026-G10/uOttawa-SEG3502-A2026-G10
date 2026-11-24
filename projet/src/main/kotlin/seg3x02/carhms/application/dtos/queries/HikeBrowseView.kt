package seg3x02.carhms.application.dtos.queries

data class HikeBrowseView(
    val hikes: List<HikeSummaryDto>,
    val recommendedHikeIds: Set<String>
)

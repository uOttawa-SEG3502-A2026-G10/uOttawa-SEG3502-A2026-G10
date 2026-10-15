package seg3x02.carhms.application.dtos.queries

data class CancelHikeQuery(
    val organizerEmail: String,
    val hikeId: String,
    val reason: String
)

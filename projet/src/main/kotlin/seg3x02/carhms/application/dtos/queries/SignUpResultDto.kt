package seg3x02.carhms.application.dtos.queries

data class SignUpResultDto(
    val hikeId: String,
    val status: String,
    val waitlistPosition: Int? = null
)

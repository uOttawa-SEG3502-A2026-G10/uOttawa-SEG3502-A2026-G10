package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.SignUpForHikeQuery
import seg3x02.carhms.application.dtos.queries.SignUpResultDto

interface SignUpForHike {
    fun execute(query: SignUpForHikeQuery): SignUpResultDto
}

package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.CreateHikeQuery
import seg3x02.carhms.application.dtos.queries.HikeIdDto

interface CreateHike {
    fun execute(query: CreateHikeQuery): HikeIdDto
}

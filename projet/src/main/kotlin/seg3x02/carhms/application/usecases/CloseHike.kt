package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.CloseHikeQuery

interface CloseHike {
    fun execute(query: CloseHikeQuery): Unit
}

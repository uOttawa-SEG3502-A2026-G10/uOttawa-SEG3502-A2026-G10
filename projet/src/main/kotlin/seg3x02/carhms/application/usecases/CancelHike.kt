package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.CancelHikeQuery

interface CancelHike {
    fun execute(query: CancelHikeQuery): Unit
}

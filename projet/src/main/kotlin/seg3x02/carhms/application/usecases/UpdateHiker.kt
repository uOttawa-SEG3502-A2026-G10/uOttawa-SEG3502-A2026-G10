package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.UpdateHikerQuery

interface UpdateHiker {
    fun execute(query: UpdateHikerQuery): Unit
}

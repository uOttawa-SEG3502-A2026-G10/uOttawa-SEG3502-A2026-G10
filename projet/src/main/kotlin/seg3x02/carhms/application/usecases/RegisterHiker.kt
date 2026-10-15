package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.HikerIdDto
import seg3x02.carhms.application.dtos.queries.RegisterHikerQuery

interface RegisterHiker {
    fun execute(query: RegisterHikerQuery): HikerIdDto
}

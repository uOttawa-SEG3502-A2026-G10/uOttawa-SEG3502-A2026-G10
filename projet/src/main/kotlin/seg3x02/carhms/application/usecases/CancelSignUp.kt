package seg3x02.carhms.application.usecases

import seg3x02.carhms.application.dtos.queries.CancelSignUpQuery

interface CancelSignUp {
    fun execute(query: CancelSignUpQuery): Unit
}

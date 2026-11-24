package seg3x02.carhms.application.services

import seg3x02.carhms.domain.authentication.Principal

interface UserContext {
    fun current(): Principal?
}

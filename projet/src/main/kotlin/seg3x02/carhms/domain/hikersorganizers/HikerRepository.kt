package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Email

interface HikerRepository {
    fun find(email: Email): Hiker?
    fun save(hiker: Hiker)
}

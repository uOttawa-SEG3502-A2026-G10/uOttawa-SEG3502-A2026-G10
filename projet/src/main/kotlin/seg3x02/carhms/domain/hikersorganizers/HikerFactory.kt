package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Email

interface HikerFactory {
    fun create(email: Email, passwordHash: String, profile: HikerProfile, waiver: Waiver): Hiker
}

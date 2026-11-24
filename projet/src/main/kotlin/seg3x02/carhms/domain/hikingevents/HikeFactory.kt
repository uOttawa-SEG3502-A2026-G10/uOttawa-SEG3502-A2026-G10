package seg3x02.carhms.domain.hikingevents

import seg3x02.carhms.domain.Email
import java.time.Instant

interface HikeFactory {
    fun create(organizer: Email, details: HikeDetails, at: Instant): Hike
}

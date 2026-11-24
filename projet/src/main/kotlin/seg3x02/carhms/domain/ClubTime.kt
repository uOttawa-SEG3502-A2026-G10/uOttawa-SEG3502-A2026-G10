package seg3x02.carhms.domain

import java.time.ZoneId

class ClubTime private constructor() {
    companion object {
        val CLUB_ZONE: ZoneId = ZoneId.of("America/Toronto")
    }
}

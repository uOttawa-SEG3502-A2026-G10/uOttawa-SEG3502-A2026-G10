package seg3x02.carhms.domain.hikingevents

import java.time.Instant

data class LifecycleEntry(val status: HikeStatus, val actor: String, val at: Instant, val reason: String? = null)

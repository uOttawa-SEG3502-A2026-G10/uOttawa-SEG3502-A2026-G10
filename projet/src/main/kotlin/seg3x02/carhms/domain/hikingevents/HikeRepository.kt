package seg3x02.carhms.domain.hikingevents

import seg3x02.carhms.domain.HikeId

interface HikeRepository {
    fun find(id: HikeId): Hike?
    fun all(): List<Hike>
    fun save(hike: Hike)
}

package seg3x02.carhms.domain.trails

interface TrailRepository {
    fun find(id: String): Trail?
}

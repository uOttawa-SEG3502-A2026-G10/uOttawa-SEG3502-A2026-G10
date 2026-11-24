package seg3x02.carhms.domain.trails

class TrailsFacade(private val repository: TrailRepository) {
    fun get(id: String) = requireNotNull(repository.find(id)) { "CCN trail information unavailable" }
}

package seg3x02.carhms.domain

interface EventEmitter {
    fun emit(event: DomainEvent)
}

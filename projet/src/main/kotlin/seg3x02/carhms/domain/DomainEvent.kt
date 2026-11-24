package seg3x02.carhms.domain

import java.time.Instant

data class DomainEvent(
    val type: String,
    val aggregateId: String,
    val actor: String,
    val occurredAt: Instant,
    val details: Map<String, String> = emptyMap()
)

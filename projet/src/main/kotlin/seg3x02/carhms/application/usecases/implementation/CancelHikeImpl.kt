package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.CancelHike
import seg3x02.carhms.domain.*

class CancelHikeImpl(private val env: UseCaseHelpers) : CancelHike {
    override fun execute(query: CancelHikeQuery) {
        val (hike, affected) = env.transactions.run {
            val email = Email.of(query.organizerEmail)
            val principal = requireNotNull(env.users.current()) { "Authentication required" }
            require(principal.email == email) { "Actor identity mismatch" }
            val hike = env.hikes.get(HikeId(query.hikeId))
            require(principal.isAdmin() || hike.organizerEmail == email) { "Not authorized to cancel this hike" }
            if (!hike.cancel(email.value, query.reason, env.clock.instant())) return@run hike to emptyList()
            val registrations = env.registrations.cancelAll(hike.id)
            env.hikes.save(hike)
            env.lifecycle(hike)
            registrations.forEach { env.event("RegistrationCancelled", it.id, mapOf("hikeId" to hike.id.value)) }
            hike to registrations
        }
        affected.forEach { env.notify(hike, it.hikerEmail, "hike-cancelled", "Randonnée annulée : ${query.reason}") }
    }
}

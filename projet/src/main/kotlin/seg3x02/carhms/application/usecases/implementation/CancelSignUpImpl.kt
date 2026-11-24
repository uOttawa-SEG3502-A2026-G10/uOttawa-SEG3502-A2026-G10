package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.CancelSignUp
import seg3x02.carhms.domain.*

class CancelSignUpImpl(private val env: UseCaseHelpers) : CancelSignUp {
    override fun execute(query: CancelSignUpQuery) {
        val (hike, result) = env.transactions.run {
            val email = Email.of(query.hikerEmail)
            env.authorization.requireSelfOrAdmin(env.users.current(), email)
            val hike = env.hikes.get(HikeId(query.hikeId))
            hike.requireRegistrationAllowed(env.clock.instant())
            val result = env.registrations.cancel(hike.id, email)
            env.event("RegistrationCancelled", result.cancelled.id, mapOf("hikeId" to hike.id.value))
            result.promoted?.let { env.event("RegistrationPromoted", it.id, mapOf("hikeId" to hike.id.value)) }
            if (hike.updateCapacity(env.registrations.confirmed(hike.id).size, env.actor(), env.clock.instant())) env.lifecycle(hike)
            env.hikes.save(hike)
            hike to result
        }
        result.promoted?.let { env.notify(hike, it.hikerEmail, "promotion-${it.id}", "Votre inscription est maintenant confirmée.") }
    }
}

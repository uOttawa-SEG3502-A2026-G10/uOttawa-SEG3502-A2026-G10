package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.SignUpForHike
import seg3x02.carhms.domain.registrations.RegistrationStatus
import seg3x02.carhms.domain.*

class SignUpForHikeImpl(private val env: UseCaseHelpers) : SignUpForHike {
    override fun execute(query: SignUpForHikeQuery): SignUpResultDto {
        val (hike, registration, result) = env.transactions.run {
            val email = Email.of(query.hikerEmail)
            env.authorization.requireSelfOrAdmin(env.users.current(), email)
            val hike = env.hikes.get(HikeId(query.hikeId))
            hike.requireRegistrationAllowed(env.clock.instant())
            env.hikers.get(email).requireEligible(env.trails.get(hike.details.trailId).difficulty)
            val registration = env.registrations.signUp(hike.id, email, hike.details.capacity, env.clock.instant())
            env.event("RegistrationCreated", registration.id, mapOf("status" to registration.status.name, "hikeId" to hike.id.value))
            if (hike.updateCapacity(env.registrations.confirmed(hike.id).size, env.actor(), env.clock.instant())) env.lifecycle(hike)
            env.hikes.save(hike)
            val position = if (registration.status == RegistrationStatus.WAITLISTED) env.registrations.position(registration) else null
            Triple(hike, registration, SignUpResultDto(hike.id.value, registration.status.name.lowercase(), position))
        }
        env.notify(hike, registration.hikerEmail, "registration-${registration.id}",
            if (result.waitlistPosition == null) "Inscription confirmée." else "Liste d'attente : position ${result.waitlistPosition}.")
        return result
    }
}

package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.UpdateHiker
import seg3x02.carhms.domain.hikersorganizers.*
import seg3x02.carhms.domain.*

class UpdateHikerImpl(private val env: UseCaseHelpers) : UpdateHiker {
    override fun execute(query: UpdateHikerQuery) = env.transactions.run {
        val email = Email.of(query.targetHikerEmail)
        env.authorization.requireSelfOrAdmin(env.users.current(), email)
        val certificateParts = listOf(query.certificateNumber, query.certificateIssueDate,
            query.certificateExpiryDate, query.organizerLevel)
        if (query.membershipStatus != null || certificateParts.any { it != null }) {
            env.authorization.requireAdmin(env.users.current())
        }
        require(certificateParts.all { it == null } || certificateParts.all { it != null }) {
            "All certification fields are required"
        }
        val hiker = env.hikers.get(email)
        val oldProfile = hiker.profile
        val oldMembership = hiker.membership
        val oldCertification = hiker.certification
        val profile = HikerProfile(
            query.names ?: oldProfile.names,
            query.phoneNumber ?: oldProfile.phone,
            query.homeAddress ?: oldProfile.address,
            query.dateOfBirth?.let(env::localDate) ?: oldProfile.birthDate,
            EmergencyContact(query.emergencyContactName ?: oldProfile.emergencyContact.name,
                query.emergencyContactPhone ?: oldProfile.emergencyContact.phone),
            query.skillLevel?.let(Level::parse) ?: oldProfile.skill
        )
        val membership = query.membershipStatus?.let {
            when (it.lowercase()) {
                "active" -> MembershipStatus.ACTIVE
                "expired", "expirée", "expiree" -> MembershipStatus.EXPIRED
                "suspended", "suspendue" -> MembershipStatus.SUSPENDED
                else -> throw IllegalArgumentException("Unknown membership status")
            }
        } ?: oldMembership
        val certificate = if (query.certificateNumber != null) {
            Certification(query.certificateNumber, env.localDate(requireNotNull(query.certificateIssueDate)),
                env.localDate(requireNotNull(query.certificateExpiryDate)), Level.parse(requireNotNull(query.organizerLevel)))
                .also { require(it.validOn(env.today())) { "Certification must currently be valid" } }
        } else oldCertification
        hiker.update(profile, membership, certificate, env.today())
        env.hikers.save(hiker)
        if (profile != oldProfile) env.event("HikerProfileUpdated", email.value)
        if (membership != oldMembership) env.event("MembershipChanged", email.value, mapOf("status" to membership.name))
        if (certificate != oldCertification) env.event("OrganizerApproved", email.value)
    }
}

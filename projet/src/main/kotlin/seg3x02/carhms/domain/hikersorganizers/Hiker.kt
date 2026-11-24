package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Email
import seg3x02.carhms.domain.Level
import java.time.LocalDate
import java.time.Period

class Hiker(
    val email: Email,
    val passwordHash: String,
    profile: HikerProfile,
    val waiver: Waiver,
    membership: MembershipStatus = MembershipStatus.ACTIVE,
    certification: Certification? = null
) {
    var profile = profile
        private set
    var membership = membership
        private set
    var certification = certification
        private set

    init {
        require(passwordHash.isNotBlank())
        validateProfile(profile, waiver.acceptedOn)
        require(Period.between(profile.birthDate, waiver.acceptedOn).years >= 18 || !waiver.guardianName.isNullOrBlank()) {
            "Guardian waiver required"
        }
    }

    fun update(profile: HikerProfile, membership: MembershipStatus, certification: Certification?, today: LocalDate) {
        validateProfile(profile, today)
        require(Period.between(profile.birthDate, waiver.acceptedOn).years >= 18 || !waiver.guardianName.isNullOrBlank()) {
            "Birth date update requires a guardian waiver"
        }
        this.profile = profile
        this.membership = membership
        this.certification = certification
    }

    fun requireEligible(difficulty: Level) {
        require(membership == MembershipStatus.ACTIVE) { "Inactive membership" }
        require(profile.skill.permits(difficulty)) { "Insufficient skill level" }
    }

    fun requireOrganizer(date: LocalDate, difficulty: Level) {
        require(membership == MembershipStatus.ACTIVE) { "Inactive organizer membership" }
        val certificate = requireNotNull(certification) { "Organizer certification required" }
        require(certificate.validOn(date)) { "Certification not valid on hike date" }
        require(certificate.level.canLead(difficulty)) { "Insufficient organizer level" }
    }

    private fun validateProfile(profile: HikerProfile, today: LocalDate) {
        require(!profile.birthDate.isAfter(today)) { "Birth date is in the future" }
    }
}

package seg3x02.carhms.application.usecases.implementation

import seg3x02.carhms.application.dtos.queries.*
import seg3x02.carhms.application.usecases.RegisterHiker
import seg3x02.carhms.domain.hikersorganizers.*
import seg3x02.carhms.domain.*
import java.time.Period

class RegisterHikerImpl(private val env: UseCaseHelpers) : RegisterHiker {
    override fun execute(query: RegisterHikerQuery): HikerIdDto = env.transactions.run {
        val email = Email.of(query.email)
        require(query.password.length >= 8) { "Password must contain at least eight characters" }
        val birthDate = env.localDate(query.dateOfBirth)
        require(!birthDate.isAfter(env.today())) { "Birth date is in the future" }
        val minor = Period.between(birthDate, env.today()).years < 18
        require(if (minor) query.guardianWaiverAccepted && !query.guardianName.isNullOrBlank() else query.waiverAccepted) {
            "Waiver acceptance required"
        }
        val profile = HikerProfile(query.names, query.phoneNumber, query.homeAddress, birthDate,
            EmergencyContact(query.emergencyContactName, query.emergencyContactPhone), Level.parse(query.skillLevel))
        val waiver = Waiver(env.today(), if (minor) query.guardianName else null)
        env.hikers.register(query.invitationCode, email, env.passwords.hash(query.password), profile, waiver)
        env.event("HikerRegistered", email.value, actor = email.value)
        env.event("MembershipActivated", email.value, actor = email.value)
        env.event("WaiverAccepted", email.value, actor = email.value)
        env.event("InvitationConsumed", query.invitationCode, actor = email.value)
        HikerIdDto(email.value)
    }
}

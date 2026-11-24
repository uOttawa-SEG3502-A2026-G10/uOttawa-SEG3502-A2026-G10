package seg3x02.carhms.domain.hikersorganizers

import seg3x02.carhms.domain.Email

class HikersFacade(
    private val repository: HikerRepository,
    private val invitations: InvitationRepository,
    private val factory: HikerFactory
) {
    fun get(email: Email) = requireNotNull(repository.find(email)) { "Unknown hiker" }
    fun register(code: String, email: Email, hash: String, profile: HikerProfile, waiver: Waiver): Hiker {
        require(repository.find(email) == null) { "Email already registered" }
        val invitation = requireNotNull(invitations.find(code)) { "Invalid invitation" }
        require(!invitation.used) { "Invitation already used" }
        val hiker = factory.create(email, hash, profile, waiver)
        invitation.consume()
        invitations.save(invitation)
        repository.save(hiker)
        return hiker
    }
    fun save(hiker: Hiker) = repository.save(hiker)
}

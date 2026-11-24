package seg3x02.carhms.domain.hikersorganizers

interface InvitationRepository {
    fun find(code: String): Invitation?
    fun save(invitation: Invitation)
}

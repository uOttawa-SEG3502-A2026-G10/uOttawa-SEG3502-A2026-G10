package seg3x02.carhms.application.dtos.queries

import java.util.*

data class RegisterHikerQuery(
    val invitationCode: String,
    val email: String,
    val password: String,
    val names: String,
    val phoneNumber: String,
    val homeAddress: String,
    val dateOfBirth: Date,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val skillLevel: String,
    val waiverAccepted: Boolean,
    val guardianName: String? = null,
    val guardianWaiverAccepted: Boolean = false
)

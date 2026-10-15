package seg3x02.carhms.application.dtos.queries

import java.util.*

data class UpdateHikerQuery(
    val targetHikerEmail: String,
    val names: String? = null,
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val dateOfBirth: Date? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val skillLevel: String? = null,
    val membershipStatus: String? = null,
    val certificateNumber: String? = null,
    val certificateIssueDate: Date? = null,
    val certificateExpiryDate: Date? = null,
    val organizerLevel: String? = null
)

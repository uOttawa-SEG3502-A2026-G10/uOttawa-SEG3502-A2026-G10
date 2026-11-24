package seg3x02.carhms.domain.hikersorganizers

data class EmergencyContact(val name: String, val phone: String) {
    init { require(name.isNotBlank() && phone.isNotBlank()) }
}

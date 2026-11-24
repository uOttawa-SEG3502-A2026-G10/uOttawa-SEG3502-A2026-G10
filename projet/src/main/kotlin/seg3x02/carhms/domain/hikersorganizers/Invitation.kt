package seg3x02.carhms.domain.hikersorganizers

class Invitation(val code: String, used: Boolean = false) {
    var used = used
        private set
    init { require(code.isNotBlank()) }
    fun consume() {
        require(!used) { "Invitation already used" }
        used = true
    }
}

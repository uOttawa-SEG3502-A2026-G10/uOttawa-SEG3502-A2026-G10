package seg3x02.carhms.domain

data class Email(val value: String) {
    init {
        require(value == value.trim().lowercase() && value.matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            "Invalid normalized email"
        }
    }
    companion object {
        fun of(value: String) = Email(value.trim().lowercase())
    }
}

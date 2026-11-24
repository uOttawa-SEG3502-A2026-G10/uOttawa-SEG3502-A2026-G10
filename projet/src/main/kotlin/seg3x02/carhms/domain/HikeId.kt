package seg3x02.carhms.domain

data class HikeId(val value: String) {
    init { require(value.isNotBlank()) }
}

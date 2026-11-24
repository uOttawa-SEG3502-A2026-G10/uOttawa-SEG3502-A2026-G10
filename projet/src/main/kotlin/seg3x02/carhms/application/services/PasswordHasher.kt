package seg3x02.carhms.application.services

interface PasswordHasher {
    fun hash(password: String): String
}

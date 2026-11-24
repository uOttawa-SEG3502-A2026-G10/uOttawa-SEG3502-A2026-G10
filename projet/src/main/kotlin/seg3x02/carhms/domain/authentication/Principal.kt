package seg3x02.carhms.domain.authentication

import seg3x02.carhms.domain.Email

data class Principal(val email: Email?, val roles: Set<Role>) {
    fun isAdmin() = Role.ADMINISTRATOR in roles
}

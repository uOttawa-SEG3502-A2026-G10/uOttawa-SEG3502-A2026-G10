package seg3x02.carhms.domain.authentication

import seg3x02.carhms.domain.Email

class AuthorizationFacade {
    fun requireSelfOrAdmin(principal: Principal?, target: Email): Principal {
        val user = requireNotNull(principal) { "Authentication required" }
        require(user.isAdmin() || (Role.HIKER in user.roles && user.email == target)) { "Not authorized" }
        return user
    }
    fun requireAdmin(principal: Principal?) {
        require(principal?.isAdmin() == true) { "Administrator required" }
    }
    fun requireSystem(principal: Principal?) {
        require(principal != null && Role.SYSTEM in principal.roles) { "System identity required" }
    }
}

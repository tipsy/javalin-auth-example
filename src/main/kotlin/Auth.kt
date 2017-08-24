import io.javalin.Context
import io.javalin.Handler
import io.javalin.security.Role
import java.util.*

enum class ApiRole : Role { ANYONE, USER_READ, USER_WRITE }

object Auth {

    // we'll store passwords in clear text (and in memory) for this example, but please don't
    // do this if you have actual users
    // typically you would store hashed passwords in a database and validate them using using
    // something like bcrypt (http://www.mindrot.org/projects/jBCrypt/)
    private val userRoleMap = hashMapOf(
            Pair("alice", "weak-password") to listOf(ApiRole.USER_READ),
            Pair("bob", "better-password") to listOf(ApiRole.USER_READ, ApiRole.USER_WRITE)
    )

    // our access-manager is simple
    // when endpoint has Role.ANYONE, we will always handle the request
    // when the request has the permitted roles (determined by inspecting the request) we handle the request.
    // else, we set status 401
    fun accessManager(handler: Handler, ctx: Context, permittedRoles: List<Role>) {
        when {
            permittedRoles.contains(ApiRole.ANYONE) -> handler.handle(ctx)
            ctx.userRoles.containsAny(permittedRoles) -> handler.handle(ctx)
            else -> ctx.status(401).json("Unauthorized")
        }
    }

    // get username/password from context and use as key in userRoleMap
    private val Context.userRoles: List<ApiRole>
        get() {
            val usernamePasswordPair = basicAuthCredentials(this.header("Basic")) ?: return listOf() // return empty list if no credentials
            return userRoleMap[usernamePasswordPair] ?: listOf() // return role for u/p, empty list if no roles found
        }

    private fun List<Any>.containsAny(otherList: List<Any>) = !Collections.disjoint(this, otherList)

    // Returns username and password as a pair if they're present, otherwise null
    private fun basicAuthCredentials(header: String?): Pair<String, String>? = try {
        val credentials = String(Base64.getDecoder().decode(header!!.removePrefix("Basic "))).split(":")
        if (credentials.size == 2) Pair(credentials[0], credentials[1]) else null
    } catch (e: Exception) {
        null
    }

}


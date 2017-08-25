import io.javalin.Context
import io.javalin.Handler
import io.javalin.security.Role
import java.util.*

enum class ApiRole : Role { ANYONE, USER_READ, USER_WRITE }

object Auth {

    // our access-manager is simple
    // when endpoint has Role.ANYONE, we will always handle the request
    // when the request has the permitted roles (determined by inspecting the request) we handle the request.
    // else, we set status 401
    fun accessManager(handler: Handler, ctx: Context, permittedRoles: List<Role>) {
        when {
            permittedRoles.contains(ApiRole.ANYONE) -> handler.handle(ctx)
            ctx.userRoles.any { it in permittedRoles } -> handler.handle(ctx)
            else -> ctx.status(401).json("Unauthorized")
        }
    }

    // get roles from userRoleMap after extracting username/password from basic-auth header
    private val Context.userRoles: List<ApiRole>
        get() = try {
            val (username, password) = String(Base64.getDecoder().decode(this.header("Authorization")!!.removePrefix("Basic "))).split(":")
            userRoleMap[Pair(username, password)] ?: listOf() // return role for u/p, empty list if no roles found
        } catch (e: Exception) {
            listOf()
        }

    // we'll store passwords in clear text (and in memory) for this example, but please don't
    // do this if you have actual users
    // typically you would store hashed passwords in a database and validate them using using
    // something like bcrypt (http://www.mindrot.org/projects/jBCrypt/)
    private val userRoleMap = hashMapOf(
            Pair("alice", "weak-password") to listOf(ApiRole.USER_READ),
            Pair("bob", "better-password") to listOf(ApiRole.USER_READ, ApiRole.USER_WRITE)
    )

}

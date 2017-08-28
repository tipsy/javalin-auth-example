import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import io.javalin.security.Role.roles

fun main(args: Array<String>) {

    val app = Javalin.create().apply {
        port(7000)
        accessManager(Auth::accessManager)
    }.start()

    app.routes {
        get("/", { ctx -> ctx.redirect("/users") })
        path("users") {
            get(UserController::getAllUserIds, roles(ApiRole.ANYONE))
            post(UserController::createUser, roles(ApiRole.USER_WRITE))
            path(":user-id") {
                get(UserController::getUser, roles(ApiRole.USER_READ))
                patch(UserController::updateUser, roles(ApiRole.USER_WRITE))
                delete(UserController::deleteUser, roles(ApiRole.USER_WRITE))
            }
        }
    }

}

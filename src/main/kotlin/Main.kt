import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles

fun main() {

    val app = Javalin.create{
        it.accessManager(Auth::accessManager)
    }.start(7000)

    app.routes {
        get("/", { ctx -> ctx.redirect("/users") }, roles(ApiRole.ANYONE))
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

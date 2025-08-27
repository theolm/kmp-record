import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

internal class AndroidPermissionHandler(
    private val hasAudioPermission: suspend () -> Boolean,
    private val requestAudioPermission: suspend () -> Unit
) : PermissionController {
    override suspend fun hasAudioRecordingPermission(): Boolean = hasAudioPermission()

    override suspend fun requestAudioRecordingPermission() = requestAudioPermission()
}

@Composable
internal actual fun rememberPermissionController(): PermissionController {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController =
        remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    return remember {
        AndroidPermissionHandler(
            hasAudioPermission = { controller.isPermissionGranted(Permission.RECORD_AUDIO) },
            requestAudioPermission = { controller.providePermission(Permission.RECORD_AUDIO) }
        )
    }
}
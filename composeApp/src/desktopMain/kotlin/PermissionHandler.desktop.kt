import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


@Composable
internal actual fun rememberPermissionController(): PermissionController {

    return remember {
        object : PermissionController {
            override suspend fun hasAudioRecordingPermission(): Boolean {
                return true
            }

            override suspend fun requestAudioRecordingPermission() = Unit
        }
    }
}
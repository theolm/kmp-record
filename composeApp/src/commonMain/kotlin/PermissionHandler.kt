import androidx.compose.runtime.Composable


interface PermissionController {
    suspend fun hasAudioRecordingPermission(): Boolean
    suspend fun requestAudioRecordingPermission()
}

@Composable
expect internal fun rememberPermissionController(): PermissionController



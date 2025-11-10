package home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.theolm.record.ExperimentalVolumeCallback
import dev.theolm.record.Record
import dev.theolm.record.VolumeCallback
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalVolumeCallback::class)
class HomeScreen : Screen {
    @Composable
    override fun Content() {
        Screen()
    }

    @Composable
    private fun Screen() {
        val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
        val controller: PermissionsController =
            remember(factory) { factory.createPermissionsController() }
        val coroutineScope: CoroutineScope = rememberCoroutineScope()

        BindEffect(controller)

        val screenModel = rememberScreenModel { HomeScreenModel() }
        var uiState by screenModel.uiState

        LaunchedEffect(Unit) {
            Record.setConfig(
                RecordConfig(
                    outputLocation = OutputLocation.Cache,
                    outputFormat = OutputFormat.MPEG_4,
                    volumeCallback = object : VolumeCallback {
                        override fun onVolumeChanged(volume: Double) {
                            // On Volume Changed Callback
                        }
                    }
                )
            )
        }

        var recording by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (!controller.isPermissionGranted(Permission.RECORD_AUDIO)) {
                                    controller.providePermission(Permission.RECORD_AUDIO)
                                } else {
                                    recording = if (recording) {
                                        Record.stopRecording().also {
                                            println("Recording stopped. File saved at $it")
                                        }
                                        false
                                    } else {
                                        runCatching {
                                            Record.startRecording()
                                            true
                                        }.onFailure {
                                            println("Error: $it")
                                        }.getOrDefault(false)
                                    }
                                }
                            }
                        }
                    ) {
                        val text = if (recording) {
                            "Stop Recording"
                        } else {
                            "Start Recording"
                        }
                        Text(text)
                    }
                }
            }
        )
    }
}

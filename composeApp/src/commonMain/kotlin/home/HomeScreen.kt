package home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dev.theolm.record.Record

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        Screen()
    }

    @Composable
    private fun Screen() {
        val screenModel = rememberScreenModel { HomeScreenModel() }
        var uiState by screenModel.uiState

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

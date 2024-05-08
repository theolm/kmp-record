import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import home.HomeScreen

@Composable
fun App() {
    AppTheme(
        seedColor = Color.Yellow
    ) {
        Navigator(HomeScreen())
    }
}

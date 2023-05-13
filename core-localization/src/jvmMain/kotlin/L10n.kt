import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive

object L10n {

    private val default = DefaultLocalization()

    val currentLanguage = callbackFlow<String> {
        while (true) {
            if (!isActive) {
                break
            }
            trySend(get("lang"))
            delay(1000)
        }
    }

    fun setLanguage(lang: String) {
        default.setLanguage(lang)
    }

    fun get(key: String): String = default.get(key)
}

fun String.localized(): String {
    return L10n.get(this)
}

package com.example.hellowatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val UNLOCK_URL = "https://closing-skink-socially.ngrok-free.app/unlock/"

// App colors
private val ButtonBlue = Color(0xFF007BFF)
private val FlashGreen = Color(0xFF22C55E)
private val FlashRed = Color(0xFFEF4444)
private val ScreenBlack = Color(0xFF000000)

/** Intent extra: when true the screen unlocks immediately and closes afterward (tile tap). */
const val EXTRA_AUTO_UNLOCK = "auto_unlock"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val autoUnlock = intent?.getBooleanExtra(EXTRA_AUTO_UNLOCK, false) ?: false
        setContent {
            MaterialTheme {
                UnlockScreen(
                    autoUnlock = autoUnlock,
                    onAutoComplete = { finish() }
                )
            }
        }
    }
}

@Composable
fun UnlockScreen(
    autoUnlock: Boolean = false,
    onAutoComplete: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    // Target background color the screen animates toward. Flashing toggles this.
    var backgroundColor by remember { mutableStateOf(ScreenBlack) }
    var isBusy by remember { mutableStateOf(false) }

    val animatedBackground by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 150),
        label = "background"
    )

    // Runs the unlock + flash; when [thenComplete] the activity closes after the flash.
    val runUnlock: (Boolean) -> Unit = remember {
        { thenComplete ->
            if (!isBusy) {
                isBusy = true
                scope.launch {
                    val success = sendUnlockRequest()
                    flash(if (success) FlashGreen else FlashRed) { color ->
                        backgroundColor = color
                    }
                    backgroundColor = ScreenBlack
                    isBusy = false
                    if (thenComplete) onAutoComplete()
                }
            }
        }
    }

    // Tile launch: fire once on first composition.
    LaunchedEffect(Unit) {
        if (autoUnlock) runUnlock(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackground),
        contentAlignment = Alignment.Center
    ) {
        Button(
            enabled = !isBusy,
            onClick = { runUnlock(false) },
            colors = ButtonDefaults.primaryButtonColors(
                backgroundColor = ButtonBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "UNLOCK\n\nDOOR",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Flashes [color] three times by toggling the background via [setColor]. */
private suspend fun flash(color: Color, setColor: (Color) -> Unit) {
    repeat(3) {
        setColor(color)
        delay(250)
        setColor(ScreenBlack)
        delay(250)
    }
}

/** Fires the unlock POST request. Returns true on a 2xx response. */
private suspend fun sendUnlockRequest(): Boolean = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        connection = (URL(UNLOCK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
            )
            setRequestProperty("Referer", "https://192.168.0.27:8080/")
            doOutput = true
        }
        val code = connection.responseCode
        code in 200..299
    } catch (e: Exception) {
        false
    } finally {
        connection?.disconnect()
    }
}

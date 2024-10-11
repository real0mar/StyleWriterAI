package org.real0mar.stylewriterai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.real0mar.stylewriterai.ui.theme.StyleWriterAITheme
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.layout.Arrangement

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StyleWriterAITheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Text input at the top
                    var inputText by remember { mutableStateOf("") }
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter your text here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Swipeable section at the bottom
                    val styles = listOf("Hemingway", "Trump", "Shakespeare")
                    var selectedStyleIndex by remember { mutableStateOf(0) }
                    val pagerState = rememberPagerState {styles.size}

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxHeight(0.4f)
                            .padding(16.dp)
                    ) { page ->
                        Text(
                            text = styles[page],
                            modifier = Modifier.fillMaxSize(),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        selectedStyleIndex = page
                    }

                    // Button to trigger style conversion
                    Button(
                        onClick = {
                            // TODO: Call Gemini Nano model with inputText and styles[selectedStyleIndex]
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Convert Text")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StyleWriterAITheme {
        Greeting("Android")
    }
}

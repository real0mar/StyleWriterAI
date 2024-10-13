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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.real0mar.stylewriterai.ui.theme.StyleWriterAITheme
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.layout.Arrangement
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.GenerationConfig
import com.google.ai.edge.aicore.generationConfig
import com.google.ai.edge.aicore.GenerativeAIException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion

class MainActivity : ComponentActivity() {
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the generative model
        initGenerativeModel()

        setContent {
            StyleWriterAITheme {
                var inputText by remember { mutableStateOf("") }
                var outputText by remember { mutableStateOf("Converted text will appear here") }

                val styles = listOf("Hemingway", "Trump", "Shakespeare")
                var selectedStyleIndex by remember { mutableStateOf(0) }
                val pagerState = rememberPagerState { styles.size }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Text input at the top
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter your text here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Swipeable section at the bottom for selecting style
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
                        selectedStyleIndex = page  // Update the selected style index based on swipe
                    }

                    // Display the converted text output
                    Text(
                        text = outputText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Button to trigger style conversion
                    Button(
                        onClick = {
                            // Pass the selected style into the prompt correctly
                            runStreamingInference(inputText, styles[selectedStyleIndex]) { result ->
                                outputText = result
                            }
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

    private fun initGenerativeModel() {
        // Configure the generation parameters using the example structure
        val generationConfig = generationConfig {
            context = applicationContext
            temperature = 0.7f // Adjust temperature for randomness
            topK = 16 // Adjust top-k value
            maxOutputTokens = 256 // Limit the output length
        }

        generativeModel = GenerativeModel(generationConfig)
    }

    private fun runStreamingInference(inputText: String, style: String, onResult: (String) -> Unit) {
        // Create a coroutine scope for background work
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = "Please rewrite the following text in the style of $style: $inputText"
                var result = ""
                var hasFirstStreamingResult = false

                generativeModel.generateContentStream(prompt)
                    .onCompletion { /* Handle end of streaming if needed */ }
                    .collect { response ->
                        result += response.text
                        // Update the UI with the streaming result
                        if (hasFirstStreamingResult) {
                            onResult(result)
                        } else {
                            hasFirstStreamingResult = true
                            onResult(result)
                        }
                    }
            } catch (e: GenerativeAIException) {
                e.printStackTrace()
                onResult("Error: ${e.message}")
            }
        }
    }
}

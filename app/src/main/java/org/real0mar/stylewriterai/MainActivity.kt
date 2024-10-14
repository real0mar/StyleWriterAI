package org.real0mar.stylewriterai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.real0mar.stylewriterai.ui.theme.StyleWriterAITheme
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import com.google.ai.edge.aicore.GenerativeAIException
import kotlinx.coroutines.flow.onCompletion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.OutlinedTextFieldDefaults



class MainActivity : ComponentActivity() {
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.statusBarColor = Color(0xFFAAAAAA).toArgb()

        // Initialize the generative model
        initGenerativeModel()

        setContent {
            StyleWriterAITheme {
                var inputText by remember { mutableStateOf("") }
                var outputText by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var selectedStyleIndex by remember { mutableStateOf(0) }
                val coroutineScope = rememberCoroutineScope()

                val styles = listOf("Hemingway", "Jane Austen", "Shakespeare")
                val styleIcons = listOf(
                    Icons.Default.Edit,
                    Icons.Default.Edit,
                    Icons.Default.Edit
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues()) // Add padding for system bars
                        .padding(16.dp)
                ) {
                    // App Title
                    Text(
                        text = "Style Writer AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                    )

                    @OptIn(markerClass = [ExperimentalMaterial3Api::class])
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter your text here", color = Color.Black) }, // Label color
                        placeholder = { Text("Type something...", color = Color.DarkGray) }, // Placeholder color
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black, // Text color when focused
                            unfocusedTextColor = Color.Black, // Text color when not focused
                            focusedBorderColor = Color.Black, // Border color when focused
                            unfocusedBorderColor = Color.Gray, // Border color when not focused
                            cursorColor = Color.Black, // Cursor color
                            focusedLabelColor = Color.Black, // Label color when focused
                            unfocusedLabelColor = Color.DarkGray // Label color when not focused
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .heightIn(min = 56.dp, max = 200.dp)
                    )
                    TabRow(
                        selectedTabIndex = selectedStyleIndex,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        styles.forEachIndexed { index, style ->
                            Tab(
                                selected = selectedStyleIndex == index,
                                onClick = { selectedStyleIndex = index },
                                text = { Text(style) },
                                icon = {
                                    Icon(
                                        imageVector = styleIcons[index],
                                        contentDescription = style
                                    )
                                }
                            )
                        }
                    }

                    // Convert Button
                    Button(
                        onClick = {
                            isLoading = true
                            outputText = ""
                            coroutineScope.launch {
                                runInference(inputText, styles[selectedStyleIndex]) { result ->
                                    outputText = result
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text("Converting...")
                        } else {
                            Text("Convert Text")
                        }
                    }

                    // Output Text Area
                    Text(
                        text = "Converted Text:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (outputText.isNotBlank()) {
                                    Text(
                                        text = outputText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else if (isLoading) {
                                    Text(
                                        text = "Converting your text, please wait...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = "Your converted text will appear here.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initGenerativeModel() {
        val generationConfig = generationConfig {
            context = applicationContext
            temperature = 0.7f
            topK = 16
            maxOutputTokens = 256
        }
        generativeModel = GenerativeModel(generationConfig)
    }

    private suspend fun runInference(inputText: String, style: String, onResult: (String) -> Unit) {
        try {
            val prompt = "Rewrite the following text in the style of $style: $inputText"
            var result = ""

            generativeModel.generateContentStream(prompt)
                .onCompletion { /* Handle completion */ }
                .collect { response ->
                    result += response.text
                    onResult(result.trim())

                }
        } catch (e: GenerativeAIException) {
            e.printStackTrace()
            onResult("Error: ${e.message}")
        }
    }
}
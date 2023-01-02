package com.mrwhoknows.findanagrams

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrwhoknows.findanagrams.ui.theme.FindAnagramsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setup()
        setContent {
            FindAnagramsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (val state = viewModel.state.collectAsState().value) {
                            is Resource.Failure -> {
                                Log.d(TAG, "onCreate: Failure: ${state.msg}")
                            }
                            is Resource.Loading -> {
                                Log.d(TAG, "onCreate: loading")
                            }
                            is Resource.Success -> {
                                Log.d(TAG, "onCreate: success: ${state.data}")
                                state.data?.let {
                                    val goodWord = it.goodWord ?: ""
                                    val guessedWords = it.guessedWords
                                    Log.d(
                                        TAG, "onCreate: anagramState: $it"
                                    )
                                    EditTx(goodWord) { word ->
                                        viewModel.isGoodWord(goodWord, word)
                                    }
                                    LazyColumn {
                                        items(guessedWords.size) { index ->
                                            guessedWords[index].apply {
                                                AnagramItem(name = first, isValid = second)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnagramItem(name: String, isValid: Boolean) {
    Text(
        text = "$name!", color = if (isValid) Color.Green else Color.Red
    )
}

@Composable
fun EditTx(
    word: String, onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        modifier = Modifier.padding(4.dp),
        label = { Text(text = word) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            onSubmit.invoke(text.trim())
        })
    )
}

private const val TAG = "MainActivity"
package com.mrwhoknows.findanagrams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.Reader
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val reader: Reader
) : ViewModel() {
    private val wordToAnagrams = hashMapOf<String, MutableList<String>>()
    private val wordSet = hashSetOf<String>()
    private val wordList = mutableListOf<String>()

    private var currentState: AnagramViewState = AnagramViewState()
    private val _state: MutableStateFlow<Resource<AnagramViewState>> =
        MutableStateFlow(Resource.Loading())
    val state = _state.asStateFlow()

    fun getAnagrams(sourceWord: String): Set<String> {
        val result = hashSetOf<String>()
        val sourceWordSorted = sourceWord.sortLetters()
        for (word in wordSet) {
            if (sourceWordSorted.equals(word.sortLetters(), true)) {
                result.add(word)
            }
        }
        wordToAnagrams[sourceWord] = result.toMutableList()
        return result
    }

    fun pickGoodStarterWord(minimumAnagram: Int = 4): String {
        var toContinue = true
        var word = ""
        while (toContinue) {
            val randomIndex = Random.nextInt(wordSet.size)
            word = wordList[randomIndex]
            val sortedWord = word.sortLetters()
            val anagrams = wordToAnagrams[word] ?: mutableListOf()
            val noOfAnagrams =
                if (anagrams.isNotEmpty()) anagrams.size else getAnagrams(sortedWord).size
            toContinue = noOfAnagrams <= minimumAnagram
        }
        return word
    }
    fun isGoodWord(baseWord: String, enteredWord: String) {
        val isGoodWord =
            !baseWord.equals(enteredWord, true) && getAnagrams(baseWord).contains(enteredWord)
        val previousList = currentState.guessedWords.toMutableList()
        previousList.add(Pair(enteredWord, isGoodWord))
        currentState = currentState.copy(guessedWords = previousList)
        viewModelScope.launch {
            _state.emit(Resource.Success(currentState))
        }
    }

    private var setupJob: Job? = null
    fun setup() {
        setupJob?.cancel()
        setupJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.emit(Resource.Loading())
                val input = BufferedReader(reader).readLines()
                input.forEach {
                    val word = it.trim()
                    wordSet.add(word)
                    val sortedWord = word.sortLetters()
                    if (wordToAnagrams.containsKey(sortedWord) && !wordToAnagrams[sortedWord].isNullOrEmpty()) {
                        wordToAnagrams[sortedWord]?.add(word)
                    } else {
                        wordToAnagrams[sortedWord] = mutableListOf(word)
                    }
                }
                reader.close()
                wordList.addAll(wordSet)
                val goodWord = pickGoodStarterWord()
                currentState = currentState.copy(
                    goodWord = goodWord,
                    guessedWords = emptyList(),
                )
                _state.emit(Resource.Success(currentState))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.emit(Resource.Failure())
            }
        }
    }

    private fun String.sortLetters(): String = String(this.toCharArray().also { it.sort() })
}

data class AnagramViewState(
    val goodWord: String? = null,
    val guessedWords: List<Pair<String, Boolean>> = emptyList(),
)

sealed class Resource<T> {
    data class Failure<T>(val msg: String = "Something Went Wrong!") : Resource<T>()
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T?) : Resource<T>()
}

private const val TAG = "MainViewModel"
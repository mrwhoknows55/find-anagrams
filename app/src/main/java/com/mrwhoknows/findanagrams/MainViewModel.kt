package com.mrwhoknows.findanagrams

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.BufferedReader
import java.io.Reader
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val reader: Reader
) : ViewModel() {
    private val wordSet = hashSetOf<String>()
    fun setup() {
        val words = BufferedReader(reader).readLines()
        wordSet.addAll(words)
        Log.d(TAG, "words: $words")
    }
}

private const val TAG = "MainViewModel"
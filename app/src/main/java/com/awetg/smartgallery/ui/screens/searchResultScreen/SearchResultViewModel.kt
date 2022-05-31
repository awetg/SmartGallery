package com.awetg.smartgallery.ui.screens.searchResultScreen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SearchResultViewModel: ViewModel() {

    private val _searchTextState: MutableState<String> = mutableStateOf(value = "")
    val searchTextState: State<String> = _searchTextState

    fun updateSearchTextState (newValue: String) {
        _searchTextState.value = newValue
    }
}
package com.example.ubbbicicletas.ui.notifications

import RetrofitClient
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import models.Registro

class NotificationsViewModel : ViewModel() {

    private val _activeRegistros = MutableLiveData<List<Registro>>()
    val activeRegistros: LiveData<List<Registro>> get() = _activeRegistros

    private val _inactiveRegistros = MutableLiveData<List<Registro>>()
    val inactiveRegistros: LiveData<List<Registro>> get() = _inactiveRegistros

    fun fetchRegistroByIdUser(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRegistroByIdUser(userId)
                if (response.isSuccessful) {
                    response.body()?.let { registroResponse ->
                        val activeList = registroResponse.data.filter { it.deleted != true }
                        val inactiveList = registroResponse.data.filter { it.deleted == true }
                        _activeRegistros.postValue(activeList)
                        _inactiveRegistros.postValue(inactiveList)
                        Log.d("NotificationsViewModel", "Fetch successful: ${registroResponse.data.size} registros fetched")
                    } ?: run {
                        Log.e("NotificationsViewModel", "Response body is null")
                    }
                } else {
                    Log.e("NotificationsViewModel", "Fetch failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Exception occurred: ${e.message}", e)
            }
        }
    }
}

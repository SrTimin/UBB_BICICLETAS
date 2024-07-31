package com.example.ubbbicicletas.ui.home

import RetrofitClient
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import models.BicicletaResponse
import models.UserResponse
import retrofit2.HttpException

class HomeViewModel : ViewModel() {

    private val _userResponse = MutableLiveData<UserResponse>()
    val userResponse: LiveData<UserResponse> get() = _userResponse

    private val _bicicletaResponse = MutableLiveData<BicicletaResponse>()
    val bicicletaResponse: LiveData<BicicletaResponse> get() = _bicicletaResponse

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllUsers()
                if (response.isSuccessful) {
                    response.body()?.let { _userResponse.postValue(it) }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("HomeViewModel", "Error en la respuesta de fetchUsers: $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("HomeViewModel", "HttpException en fetchUsers: ${e.message()}", e)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción en fetchUsers: ${e.message}", e)
            }
        }
    }

    fun fetchBicicletaById(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Fetching bicicleta for user ID: $userId")
                val response = RetrofitClient.instance.getBicicletaById(userId)
                if (response.isSuccessful) {
                    response.body()?.let { _bicicletaResponse.postValue(it) }
                    Log.d("HomeViewModel", "Bicicleta response: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("HomeViewModel", "Error en la respuesta de fetchBicicletaById: $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("HomeViewModel", "HttpException en fetchBicicletaById: ${e.message()}", e)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción en fetchBicicletaById: ${e.message}", e)
            }
        }
    }

}

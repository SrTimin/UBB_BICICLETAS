package com.example.ubbbicicletas.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {

    }
    val text: LiveData<String> = _text



    fun recibirResultadoQR(resultado: String) {
        // Aquí manejas el resultado del escaneo del código QR
        _text.value = resultado
    }


}
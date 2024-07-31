package com.example.ubbbicicletas.ui.dashboard

import RetrofitClient
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ubbbicicletas.database.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Bicicleta
import models.Contacto
import models.RegistroFindUser
import models.RegistroRequest
import models.User
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    private val _registroCompleto = MutableLiveData<Boolean>()
    val registroCompleto: LiveData<Boolean> = _registroCompleto

    private val _mensajeRegistro = MutableLiveData<String>()
    val mensajeRegistro: LiveData<String> = _mensajeRegistro

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _dialogMessage = MutableLiveData<String>()
    val dialogMessage: LiveData<String> = _dialogMessage

    private val _showRetireDialog = MutableLiveData<Boolean>()
    val showRetireDialog: LiveData<Boolean> = _showRetireDialog

    private val _retireDialogMessage = MutableLiveData<String>()
    val retireDialogMessage: LiveData<String> = _retireDialogMessage

    private val apiService: ApiService = RetrofitClient.instance
    private var registroJob: Job? = null

    fun recibirResultadoQR(resultado: String) {
        _text.value = resultado
        val user = getUserFromPreferences()
        val bici = getBikeFromPreferences()
        Log.d("RegistroQR", "Usuario: $user")
        Log.d("RegistroQR", "Bicicleta: $bici")
        if (user != null) {
            if (bici != null) {
                checkAndRegisterQR(resultado, user, false, bici)
            }
        } else {
            _dialogMessage.value = "Usuario no encontrado"
            _registroCompleto.value = true
        }
    }

    private fun checkAndRegisterQR(resultado: String, user: User, deleted: Boolean, bicicleta: Bicicleta) {
        registroJob?.cancel()
        Log.d("RegistroQR", "Iniciando nueva tarea de verificación")
        registroJob = viewModelScope.launch {
            try {
                Log.d("RegistroQR", "Iniciando verificación para usuario: ${user._id}")
                val checkResponse = withContext(Dispatchers.IO) {
                    apiService.findRegistroByIdUser(RegistroFindUser(user._id))
                }

                Log.d("RegistroQR", "Respuesta de verificación: ${checkResponse.code()}")
                Log.d("RegistroQR", "Cuerpo de respuesta: ${checkResponse.body()}")

                when (checkResponse.code()) {
                    200 -> {
                        Log.d("RegistroQR", "Registro existente encontrado")
                        _text.postValue("Registro existente encontrado")
                        showRetireDialog(user._id.toString())
                    }
                    204 -> {
                        Log.d("RegistroQR", "No se encontró registro existente, procediendo a crear uno nuevo")
                        registrarQR(resultado, user, deleted, bicicleta)
                    }
                    else -> {
                        Log.e("RegistroQR", "Error inesperado en la verificación: ${checkResponse.code()}")
                        _text.postValue("Error en la verificación: ${checkResponse.code()}")
                        _dialogMessage.postValue("Error en la verificación: ${checkResponse.code()}")
                    }
                }
            } catch (e: CancellationException) {
                Log.w("RegistroQR", "Operación de verificación cancelada", e)
                _text.postValue("Operación de verificación cancelada")
                _dialogMessage.postValue("Operación de verificación cancelada")
            } catch (e: Exception) {
                Log.e("RegistroQR", "Excepción en la verificación: ${e.message}", e)
                _text.postValue("Excepción en la verificación: ${e.message}")
                _dialogMessage.postValue("Error en la verificación: ${e.message}")
            } finally {
                _registroCompleto.postValue(true)
            }
        }
    }

    private fun showRetireDialog(userId: String) {
        _showRetireDialog.postValue(true)
        _retireDialogMessage.postValue("¿Desea retirar su bicicleta/scooter?")
    }

    fun onRetireConfirm(confirmed: Boolean, userId: String) {
        if (confirmed) {
            deleteRegistro(userId)
        } else {
            _text.postValue("Operación cancelada")
            _snackbarMessage.postValue("Operación cancelada")
        }
    }

    private fun deleteRegistro(userId: String) {
        viewModelScope.launch {
            try {
                val deleteResponse = withContext(Dispatchers.IO) {
                    apiService.deleteRegistro(userId)
                }

                when (deleteResponse.code()) {
                    200 -> {
                        Log.d("RegistroQR", "Registro eliminado exitosamente")
                        _text.postValue("Registro eliminado exitosamente")
                        _snackbarMessage.postValue("Bicicleta/scooter retirada exitosamente")
                    }
                    else -> {
                        Log.e("RegistroQR", "Error al eliminar registro: ${deleteResponse.code()}")
                        _text.postValue("Error al eliminar registro: ${deleteResponse.code()}")
                        _dialogMessage.postValue("Error al eliminar registro: ${deleteResponse.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("RegistroQR", "Excepción al eliminar registro: ${e.message}", e)
                _text.postValue("Error al eliminar registro: ${e.message}")
                _dialogMessage.postValue("Error al eliminar registro: ${e.message}")
            }
        }
    }

    private fun registrarQR(resultado: String, user: User, deleted: Boolean, bicicleta: Bicicleta) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDate = Date()


        val registroRequest = RegistroRequest(user,currentDate , resultado, deleted, bicicleta)

        Log.d("RegistroRequest", registroRequest.toString())

        viewModelScope.launch {
            try {
                Log.d("RegistroQR", "Iniciando creación de nuevo registro")
                val response = withContext(Dispatchers.IO) {
                    apiService.createRegistro(registroRequest)
                }
                if (response.isSuccessful) {
                    _text.postValue("Registro exitoso: ${response.body()}")
                    _snackbarMessage.postValue("Registro exitoso")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _text.postValue("Error en el registro: $errorBody")
                    _dialogMessage.postValue("Error en el registro: $errorBody")
                }
            } catch (e: CancellationException) {
                Log.w("RegistroQR", "Operación de registro cancelada", e)
                _text.postValue("Operación de registro cancelada")
                _dialogMessage.postValue("Operación de registro cancelada")
            } catch (e: Exception) {
                Log.e("RegistroQR", "Excepción en el registro: ${e.message}", e)
                _text.postValue("Excepción en el registro: ${e.message}")
                _dialogMessage.postValue("Error en el registro: ${e.message}")
            } finally {
                _registroCompleto.postValue(true)
            }
        }
    }

    private fun getUserFromPreferences(): User? {
        val sharedPref = getApplication<Application>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userIdString = sharedPref.getString("user_id", null)
        val userNombre = sharedPref.getString("user_nombre", null)
        val userIngresoString = sharedPref.getString("user_ingreso", null)
        val userCarrera = sharedPref.getString("user_carrera", null)
        val userNumero = sharedPref.getString("user_numero", null)?.toIntOrNull()
        val userCorreo = sharedPref.getString("user_correo", null)
        val userCorreoPersonal = sharedPref.getString("user_correoPersonal", null)

        if (userIdString == null || userNombre == null || userIngresoString == null || userCarrera == null || userCorreo == null) {
            Log.e("DashboardViewModel", "Faltan datos en SharedPreferences para el usuario")
            return null
        }

        Log.d("DashboardViewModel", "User ID: $userIdString")
        Log.d("DashboardViewModel", "User Name: $userNombre")
        Log.d("DashboardViewModel", "User Ingreso: $userIngresoString")
        Log.d("DashboardViewModel", "User Carrera: $userCarrera")
        Log.d("DashboardViewModel", "User Numero: $userNumero")
        Log.d("DashboardViewModel", "User Correo: $userCorreo")
        Log.d("DashboardViewModel", "User CorreoPersonal: $userCorreoPersonal")

        val userId = ObjectId(userIdString)
        val userIngreso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(userIngresoString) ?: return null
        val contacto = Contacto(userNumero, userCorreo, userCorreoPersonal)

        return User(userId, userNombre, userIngreso, userCarrera, contacto)
    }

    private fun getBikeFromPreferences(): Bicicleta? {
        val sharedPref = getApplication<Application>().getSharedPreferences("bicicleta_prefs", Context.MODE_PRIVATE)
        val bikeIdString = sharedPref.getString("bicicleta_id", null)
        val bikeUserIdString = sharedPref.getString("bicicleta_user_id", null)
        val bikeMarca = sharedPref.getString("bicicleta_marca", null)
        val bikeModelo = sharedPref.getString("bicicleta_modelo", null)
        val bikeColor = sharedPref.getString("bicicleta_color", null)

        if (bikeIdString == null || bikeUserIdString == null || bikeMarca == null || bikeModelo == null || bikeColor == null) {
            Log.e("DashboardViewModel", "Faltan datos en SharedPreferences para la bicicleta")
            return null
        }

        Log.d("DashboardViewModel", "Bike ID: $bikeIdString")
        Log.d("DashboardViewModel", "Bike User ID: $bikeUserIdString")
        Log.d("DashboardViewModel", "Bike Marca: $bikeMarca")
        Log.d("DashboardViewModel", "Bike Modelo: $bikeModelo")
        Log.d("DashboardViewModel", "Bike Color: $bikeColor")

        val bikeId = ObjectId(bikeIdString)
        val bikeUserId = ObjectId(bikeUserIdString)

        return Bicicleta(bikeId, bikeUserId, bikeMarca, bikeModelo, bikeColor)
    }



    override fun onCleared() {
        super.onCleared()
        registroJob?.cancel()
    }
}

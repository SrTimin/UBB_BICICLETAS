package com.example.ubbbicicletas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ubbbicicletas.ui.dashboard.DashboardViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator

class QRScannerActivity : AppCompatActivity() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        progressBar = findViewById(R.id.progressBar)
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Observadores de LiveData
        viewModel.registroCompleto.observe(this) { completo ->
            if (completo) {
                progressBar.visibility = View.GONE
                Log.d("Registro", "Registro completo")
            }
        }

        viewModel.mensajeRegistro.observe(this) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                mostrarDialogoResultado(mensaje)
            }
        }

        viewModel.snackbarMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                mostrarSnackbar(message)
            }
        }

        viewModel.dialogMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                mostrarDialogoResultado(message)
            }
        }

        viewModel.showRetireDialog.observe(this) { show ->
            if (show) {
                showRetireDialog()
            }
        }

        iniciarEscaneoQR()
    }

    private fun iniciarEscaneoQR() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea un código QR de estacionamiento")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.setCaptureActivity(CaptureActivityPortrait::class.java)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Snackbar.make(findViewById(android.R.id.content), "Escaneo cancelado", Snackbar.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, Snackbar.LENGTH_LONG.toLong())
            } else {
                if (esCodigoQRValido(result.contents)) {
                    mostrarDialogoConfirmacion(result.contents)
                } else {
                    mostrarDialogoError()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            finish()
        }
    }

    private fun esCodigoQRValido(contenido: String): Boolean {
        val regex = Regex("^UBB-BICI-Estacionamiento:\\d+$")
        return regex.matches(contenido)
    }

    private fun mostrarDialogoError() {
        AlertDialog.Builder(this)
            .setTitle("Código QR Inválido")
            .setMessage("El código QR escaneado es erróneo.")
            .setPositiveButton("Volver a intentar") { _, _ ->
                iniciarEscaneoQR()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showRetireDialog() {
        val userId = getUserIdFromPreferences() // Implementa esta función para obtener el ID del usuario
        AlertDialog.Builder(this)
            .setTitle("Ya tienes un registro!")
            .setMessage("Usted ya tiene un registro. ¿Desea retirar la bicicleta/scooter del estacionamiento?")
            .setPositiveButton("Retirar") { _, _ ->
                viewModel.onRetireConfirm(true, userId)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                viewModel.onRetireConfirm(false, userId)
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoConfirmacion(contenidoQR: String) {
        AlertDialog.Builder(this)
            .setTitle("Código QR Válido")
            .setMessage("¿Desea registrar este estacionamiento?")
            .setPositiveButton("Registrar") { _, _ ->
                progressBar.visibility = View.VISIBLE
                viewModel.recibirResultadoQR(contenidoQR)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoResultado(mensaje: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Resultado")
            .setMessage(mensaje)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .create()

        dialog.show()

        // Center the dialog
        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
    }

    private fun mostrarSnackbar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, Toast.LENGTH_LONG.toLong())
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", "") ?: ""
    }

}

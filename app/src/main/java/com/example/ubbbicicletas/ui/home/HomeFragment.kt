package com.example.ubbbicicletas.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ubbbicicletas.databinding.FragmentHomeBinding
import models.Bicicleta
import models.User
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.userResponse.observe(viewLifecycleOwner, Observer { userResponse ->
            binding.progressBar.visibility = View.GONE
            userResponse?.let { response ->
                if (response.result.isNotEmpty()) {
                    val user = response.result[0]
                    binding.nombre.text = user.nombre
                    binding.ingreso.text = "Año-Periodo Ingreso: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(user.ingreso)}"
                    binding.carrera.text = user.carrera
                    binding.numero.text = user.contacto.numero?.toString() ?: "N/A"
                    binding.correo.text = user.contacto.correo
                    binding.correoPersonal.text = user.contacto.correoPersonal ?: "N/A"

                    // Guarda los datos del usuario en SharedPreferences
                    saveUserToPreferences(user)

                    // Llama a la API para obtener la bicicleta del usuario
                    Log.d("HomeFragment", "Fetching bicicleta for user ID: ${user._id}")
                    binding.progressBar.visibility = View.VISIBLE
                    homeViewModel.fetchBicicletaById(user._id.toHexString())
                }
            }
        })

        homeViewModel.bicicletaResponse.observe(viewLifecycleOwner, Observer { bicicletaResponse ->
            binding.progressBar.visibility = View.GONE
            bicicletaResponse?.let { response ->
                Log.d("HomeFragment", "Received bicicleta response: $response")
                if (response.data.isNotEmpty()) {
                    val bicicleta = response.data[0]
                    binding.bicicleta.text = bicicleta.modelo

                    saveBicicletaToPreferences(bicicleta)
                    Log.d("HomeFragment", "Bicicleta saved to SharedPreferences" )
                } else {
                    binding.bicicleta.text = "No data available"
                }
            } ?: run {
                Log.d("HomeFragment", "Bicicleta response is null")
            }
        })

        // Llama a la API cuando el fragmento se crea
        binding.progressBar.visibility = View.VISIBLE
        homeViewModel.fetchUsers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveUserToPreferences(user: User) {
        Log.d("HomeFragment", "Saving user to SharedPreferences")
        val sharedPref = activity?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("user_id", user._id.toHexString())
            putString("user_nombre", user.nombre)
            putString("user_ingreso", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(user.ingreso))
            putString("user_carrera", user.carrera)
            putString("user_numero", user.contacto.numero?.toString())
            putString("user_correo", user.contacto.correo)
            putString("user_correoPersonal", user.contacto.correoPersonal)
            apply()
        }
        Log.d("HomeFragment", "User saved to SharedPreferences")
    }

    private fun saveBicicletaToPreferences(bicicleta: Bicicleta) {
        Log.d("HomeFragment", "Saving bicicleta to SharedPreferences")
        val sharedPref = activity?.getSharedPreferences("bicicleta_prefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("bicicleta_id", bicicleta._id.toHexString())
            putString("bicicleta_user_id", bicicleta.user_id.toHexString())
            putString("bicicleta_marca", bicicleta.marca)
            putString("bicicleta_modelo", bicicleta.modelo)
            putString("bicicleta_color", bicicleta.color)
            apply()
        }
        Log.d("HomeFragment", "Bicicleta saved to SharedPreferences")
    }
}

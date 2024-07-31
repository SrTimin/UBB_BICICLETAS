package com.example.ubbbicicletas.ui.notifications

import RegistroAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ubbbicicletas.databinding.FragmentNotificationsBinding
import models.Registro

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModelData()

        // Llama a la API cuando el fragmento se crea
        refreshData()

        return root
    }

    private fun setupRecyclerViews() {
        binding.recyclerViewActive.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewInactive.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun observeViewModelData() {
        notificationsViewModel.activeRegistros.observe(viewLifecycleOwner, Observer { activeRegistros ->
            updateActiveRegistros(activeRegistros)
        })

        notificationsViewModel.inactiveRegistros.observe(viewLifecycleOwner, Observer { inactiveRegistros ->
            updateInactiveRegistros(inactiveRegistros)
        })
    }

    private fun refreshData() {
        binding.progressBar.visibility = View.VISIBLE
        val userId = getUserIdFromPreferences()
        notificationsViewModel.fetchRegistroByIdUser(userId)
    }

    private fun updateActiveRegistros(activeRegistros: List<Registro>) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
        if (activeRegistros.isNotEmpty()) {
            binding.recyclerViewActive.adapter = RegistroAdapter(activeRegistros)
            binding.recyclerViewActive.visibility = View.VISIBLE
            binding.textNoActiveRecords.visibility = View.GONE
        } else {
            binding.recyclerViewActive.visibility = View.GONE
            binding.textNoActiveRecords.visibility = View.VISIBLE
        }
    }

    private fun updateInactiveRegistros(inactiveRegistros: List<Registro>) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
        if (inactiveRegistros.isNotEmpty()) {
            binding.recyclerViewInactive.adapter = RegistroAdapter(inactiveRegistros)
            binding.recyclerViewInactive.visibility = View.VISIBLE
            binding.textNoInactiveRecords.visibility = View.GONE
        } else {
            binding.recyclerViewInactive.visibility = View.GONE
            binding.textNoInactiveRecords.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPref = activity?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("user_id", "") ?: ""
    }
}
package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentCuentaCreadaBinding
import com.google.firebase.auth.FirebaseAuth

class CuentaCreadaFragment : Fragment() {

    private var _binding: FragmentCuentaCreadaBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuentaCreadaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar nombre del usuario
        val nombre = auth.currentUser?.displayName?.split(" ")?.firstOrNull()
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: "Usuario"
        binding.tvTitulo.text = "¡Todo listo, $nombre!"

        // Ir al Dashboard
        binding.btnIrDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_cuenta_creada_to_dashboard)
        }

        // Agregar primer producto
        binding.cardAgregarProducto.setOnClickListener {
            findNavController().navigate(R.id.action_cuenta_creada_to_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
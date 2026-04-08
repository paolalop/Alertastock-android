package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentNuevaContrasenaBinding
import com.google.android.material.snackbar.Snackbar

class NuevaContrasenaFragment : Fragment() {

    private var _binding: FragmentNuevaContrasenaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaContrasenaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAtras.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCambiar.setOnClickListener {
            val nueva = binding.etNuevaContrasena.text.toString()
            val confirmar = binding.etConfirmarContrasena.text.toString()

            // Validar que no estén vacíos
            if (nueva.isBlank() || confirmar.isBlank()) {
                mostrarError("Completa todos los campos")
                return@setOnClickListener
            }

            // Validar que coincidan
            if (nueva != confirmar) {
                mostrarError("Las contrasenas no coinciden")
                return@setOnClickListener
            }

            // Validar longitud mínima
            if (nueva.length < 8) {
                mostrarError("Minimo 8 caracteres")
                return@setOnClickListener
            }

            // Todo bien — simular cambio exitoso
            viewModel.cambiarContrasena(nueva)
        }

        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is AuthEstado.Cargando -> {
                    binding.btnCambiar.isEnabled = false
                    binding.progressCambiar.visibility = View.VISIBLE
                }
                is AuthEstado.Exitoso -> {
                    binding.progressCambiar.visibility = View.GONE
                    findNavController().navigate(R.id.action_nueva_to_login)
                }
                is AuthEstado.Error -> {
                    binding.btnCambiar.isEnabled = true
                    binding.progressCambiar.visibility = View.GONE
                    mostrarError(estado.mensaje)
                    viewModel.resetearEstado()
                }
                else -> {
                    binding.btnCambiar.isEnabled = true
                    binding.progressCambiar.visibility = View.GONE
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.red))
            .setTextColor(requireContext().getColor(R.color.text_primary))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
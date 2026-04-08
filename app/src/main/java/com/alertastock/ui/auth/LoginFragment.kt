package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarValidaciones()
        configurarBotones()
        observarEstado()
    }

    private fun configurarValidaciones() {
        // Valida el email mientras el usuario escribe
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.validarEmail(text.toString())
        }

        // Cambia el color del borde cuando el email es válido
        viewModel.emailValido.observe(viewLifecycleOwner) { valido ->
            binding.tilEmail.boxStrokeColor = if (valido)
                requireContext().getColor(R.color.blue)
            else
                requireContext().getColor(R.color.border_soft)
        }
    }

    private fun configurarBotones() {
        // Botón principal de login
        binding.btnIngresar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString()
            viewModel.iniciarSesion(email, contrasena)
        }

        // Ir a registro
        binding.tvRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        // Ir a recuperar contraseña
        binding.tvOlvideContrasena.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_olvide)
        }
    }

    private fun observarEstado() {
        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is AuthEstado.Inactivo -> {
                    binding.btnIngresar.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                }
                is AuthEstado.Cargando -> {
                    binding.btnIngresar.isEnabled = false
                    binding.progressLogin.visibility = View.VISIBLE
                }
                is AuthEstado.Exitoso -> {
                    binding.progressLogin.visibility = View.GONE
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is AuthEstado.Error -> {
                    binding.btnIngresar.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                    mostrarError(estado.mensaje)
                    viewModel.resetearEstado()
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
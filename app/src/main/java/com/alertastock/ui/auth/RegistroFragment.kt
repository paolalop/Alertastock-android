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
import com.alertastock.databinding.FragmentRegistroBinding
import com.google.android.material.snackbar.Snackbar

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón atrás
        binding.btnAtras.setOnClickListener {
            findNavController().popBackStack()
        }

        // Indicador de fortaleza de contraseña
        binding.etContrasena.doOnTextChanged { text, _, _, _ ->
            actualizarFortaleza(text.toString())
        }

        // Botón registrarse
        binding.btnRegistrarse.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString()
            viewModel.registrar(nombre, email, contrasena)
        }

        // Observar estado
        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is AuthEstado.Cargando -> {
                    binding.btnRegistrarse.isEnabled = false
                    binding.progressRegistro.visibility = View.VISIBLE
                }
                is AuthEstado.Exitoso -> {
                    binding.progressRegistro.visibility = View.GONE
                    findNavController().navigate(R.id.action_registro_to_verificar)
                }
                is AuthEstado.Error -> {
                    binding.btnRegistrarse.isEnabled = true
                    binding.progressRegistro.visibility = View.GONE
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.red))
                        .setTextColor(requireContext().getColor(R.color.text_primary))
                        .show()
                    viewModel.resetearEstado()
                }
                else -> {
                    binding.btnRegistrarse.isEnabled = true
                    binding.progressRegistro.visibility = View.GONE
                }
            }
        }
    }

    private fun actualizarFortaleza(contrasena: String) {
        when {
            contrasena.length >= 8 && contrasena.any { it.isUpperCase() } && contrasena.any { it.isDigit() } -> {
                binding.progressFortaleza.progress = 3
                binding.progressFortaleza.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.green))
                binding.tvFortaleza.text = "Contrasena fuerte"
                binding.tvFortaleza.setTextColor(requireContext().getColor(R.color.green))
            }
            contrasena.length >= 6 -> {
                binding.progressFortaleza.progress = 2
                binding.progressFortaleza.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.yellow))
                binding.tvFortaleza.text = "Contrasena media"
                binding.tvFortaleza.setTextColor(requireContext().getColor(R.color.yellow))
            }
            contrasena.isNotEmpty() -> {
                binding.progressFortaleza.progress = 1
                binding.progressFortaleza.progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.red))
                binding.tvFortaleza.text = "Contrasena debil"
                binding.tvFortaleza.setTextColor(requireContext().getColor(R.color.red))
            }
            else -> {
                binding.progressFortaleza.progress = 0
                binding.tvFortaleza.text = "Ingresa una contrasena"
                binding.tvFortaleza.setTextColor(requireContext().getColor(R.color.text_hint))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
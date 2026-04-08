package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentOlvideContrasenaBinding
import com.google.android.material.snackbar.Snackbar

class OlvideContrasenaFragment : Fragment() {

    private var _binding: FragmentOlvideContrasenaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOlvideContrasenaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAtras.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEnviar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.recuperarContrasena(email)
        }

        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is AuthEstado.Cargando -> {
                    binding.btnEnviar.isEnabled = false
                    binding.progressEnviar.visibility = View.VISIBLE
                }
                is AuthEstado.Exitoso -> {
                    binding.progressEnviar.visibility = View.GONE
                    findNavController().navigate(R.id.action_olvide_to_otp)
                }
                is AuthEstado.Error -> {
                    binding.btnEnviar.isEnabled = true
                    binding.progressEnviar.visibility = View.GONE
                    Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.red))
                        .setTextColor(requireContext().getColor(R.color.text_primary))
                        .show()
                    viewModel.resetearEstado()
                }
                else -> {
                    binding.btnEnviar.isEnabled = true
                    binding.progressEnviar.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
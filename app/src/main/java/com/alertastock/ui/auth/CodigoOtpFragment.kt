package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentCodigoOtpBinding
import com.google.android.material.snackbar.Snackbar

class CodigoOtpFragment : Fragment() {

    private var _binding: FragmentCodigoOtpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodigoOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAtras.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnVerificar.setOnClickListener {
            val codigo = binding.etOtp1.text.toString() +
                    binding.etOtp2.text.toString() +
                    binding.etOtp3.text.toString() +
                    binding.etOtp4.text.toString() +
                    binding.etOtp5.text.toString() +
                    binding.etOtp6.text.toString()

            if (codigo.length < 6) {
                Snackbar.make(binding.root, "Ingresa los 6 digitos", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(R.color.red))
                    .setTextColor(requireContext().getColor(R.color.text_primary))
                    .show()
                return@setOnClickListener
            }

            // Navegar a nueva contraseña
            findNavController().navigate(R.id.action_otp_to_nueva_contrasena)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
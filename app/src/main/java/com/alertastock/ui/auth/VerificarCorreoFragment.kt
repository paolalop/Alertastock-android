package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentVerificarCorreoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class VerificarCorreoFragment : Fragment() {

    private var _binding: FragmentVerificarCorreoBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificarCorreoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar el correo del usuario
        val email = auth.currentUser?.email ?: ""
        binding.tvEmail.text = email

        // Botón Ya verifiqué
        binding.btnYaVerifique.setOnClickListener {
            // Recargar el usuario para verificar si ya confirmó
            auth.currentUser?.reload()?.addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        // Correo verificado — ir a cuenta creada
                        findNavController().navigate(R.id.action_verificar_to_cuenta_creada)
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Aun no has verificado tu correo. Revisa tu bandeja",
                            Snackbar.LENGTH_LONG
                        )
                            .setBackgroundTint(requireContext().getColor(R.color.yellow))
                            .setTextColor(requireContext().getColor(R.color.text_primary))
                            .show()
                    }
                }
            }
        }

        // Botón Reenviar correo
        binding.btnReenviar.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    Snackbar.make(
                        binding.root,
                        "Correo reenviado correctamente",
                        Snackbar.LENGTH_LONG
                    )
                        .setBackgroundTint(requireContext().getColor(R.color.green))
                        .setTextColor(requireContext().getColor(R.color.text_primary))
                        .show()
                }
            }
        }

        // Cambiar correo
        binding.tvCambiarCorreo.setOnClickListener {
            findNavController().popBackStack(R.id.registroFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
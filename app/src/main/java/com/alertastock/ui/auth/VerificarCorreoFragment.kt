package com.alertastock.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alertastock.R
import com.alertastock.databinding.FragmentLoginBinding

class VerificarCorreoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Por ahora navega directo al dashboard
        view.postDelayed({
            findNavController().navigate(R.id.action_verificar_to_dashboard)
        }, 2000)
    }
}
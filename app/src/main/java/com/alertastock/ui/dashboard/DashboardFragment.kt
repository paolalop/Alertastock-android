package com.alertastock.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alertastock.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarHeader()
        configurarAccesosRapidos()
    }

    private fun configurarHeader() {
        // Saludo + nombre
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = when {
            hora < 12 -> "Buenos días,"
            hora < 18 -> "Buenas tardes,"
            else      -> "Buenas noches,"
        }

        val usuario = auth.currentUser
        val nombre = usuario?.displayName?.split(" ")?.firstOrNull()
            ?: usuario?.email?.substringBefore("@")
            ?: "Usuario"

        binding.tvNombreUsuario.text = "$saludo $nombre"

        // Fecha actual
        val formato = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "CO"))
        val fechaFormateada = formato.format(Date())
        binding.tvFecha.text = fechaFormateada.replaceFirstChar { it.uppercase() }

        // Avatar con primera letra
        binding.tvAvatar.text = nombre.first().uppercase()

        // Mostrar banner si hay alertas urgentes
        // Por ahora lo ocultamos hasta conectar con Room
        binding.layoutAlertaUrgente.visibility = View.GONE
    }

    private fun configurarAccesosRapidos() {
        binding.cardInventario.setOnClickListener {
            // Navegar a productos
        }
        binding.cardEscanear.setOnClickListener {
            // Navegar a escanear
        }
        binding.cardVerAlertas.setOnClickListener {
            // Navegar a alertas
        }
        binding.cardAgregarProducto.setOnClickListener {
            // Navegar a configuración
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
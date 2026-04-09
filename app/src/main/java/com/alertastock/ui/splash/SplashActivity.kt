package com.alertastock.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alertastock.MainActivity
import com.alertastock.R
import com.alertastock.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alertastock.data.local.database.AlertaStockDatabase
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Room para que cree las tablas
        AlertaStockDatabase.getDatabase(applicationContext)

        // Animación de entrada para el ícono
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.imgLogo.startAnimation(fadeIn)

        // Pasos de carga con animación
        lifecycleScope.launch {
            delay(800)
            binding.tvPaso1.animate().alpha(1f).duration = 400

            delay(800)
            binding.tvPaso2.animate().alpha(1f).duration = 400

            delay(600)
            binding.tvPaso3.animate().alpha(1f).duration = 400

            delay(600)
            irAMain()
        }
    }

    private fun irAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
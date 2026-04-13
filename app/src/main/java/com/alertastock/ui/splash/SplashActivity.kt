package com.alertastock.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alertastock.MainActivity
import com.alertastock.R
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Room
        AlertaStockDatabase.getDatabase(applicationContext)

        // Animación de entrada
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.imgLogo.startAnimation(fadeIn)

        // Pasos de carga
        lifecycleScope.launch {
            delay(800)
            binding.tvPaso1.animate().alpha(1f).duration = 400

            delay(800)
            binding.tvPaso2.animate().alpha(1f).duration = 400

            delay(600)
            binding.tvPaso3.animate().alpha(1f).duration = 400

            delay(600)
            // Ir a MainActivity con Compose
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}
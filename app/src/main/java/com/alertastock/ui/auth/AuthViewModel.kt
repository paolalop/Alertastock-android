package com.alertastock.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Todos los estados posibles del Login
sealed class AuthEstado {
    object Inactivo : AuthEstado()
    object Cargando : AuthEstado()
    object Exitoso : AuthEstado()
    data class Error(val mensaje: String) : AuthEstado()
}

class AuthViewModel : ViewModel() {

    private val _estado = MutableLiveData<AuthEstado>(AuthEstado.Inactivo)
    val estado: LiveData<AuthEstado> = _estado

    private val _emailValido = MutableLiveData(false)
    val emailValido: LiveData<Boolean> = _emailValido

    private val _contrasenaValida = MutableLiveData(false)
    val contrasenaValida: LiveData<Boolean> = _contrasenaValida

    // LOGIN
    fun iniciarSesion(email: String, contrasena: String) {
        if (email.isBlank() || contrasena.isBlank()) {
            _estado.value = AuthEstado.Error("Completa todos los campos")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.value = AuthEstado.Error("Correo electrónico inválido")
            return
        }
        if (contrasena.length < 6) {
            _estado.value = AuthEstado.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _estado.value = AuthEstado.Cargando
            delay(1500)
            _estado.value = AuthEstado.Exitoso
        }
    }

    // REGISTRO
    fun registrar(nombre: String, email: String, contrasena: String) {
        if (nombre.isBlank() || email.isBlank() || contrasena.isBlank()) {
            _estado.value = AuthEstado.Error("Completa todos los campos")
            return
        }
        if (contrasena.length < 8) {
            _estado.value = AuthEstado.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }

        viewModelScope.launch {
            _estado.value = AuthEstado.Cargando
            delay(1500)
            _estado.value = AuthEstado.Exitoso
        }
    }

    // RECUPERAR CONTRASEÑA
    fun recuperarContrasena(email: String) {
        if (email.isBlank()) {
            _estado.value = AuthEstado.Error("Ingresa tu correo electrónico")
            return
        }

        viewModelScope.launch {
            _estado.value = AuthEstado.Cargando
            delay(1000)
            _estado.value = AuthEstado.Exitoso
        }
    }

    // VALIDAR CÓDIGO OTP
    fun validarCodigo(codigo: String) {
        if (codigo.length < 6) {
            _estado.value = AuthEstado.Error("El código debe tener 6 dígitos")
            return
        }

        viewModelScope.launch {
            _estado.value = AuthEstado.Cargando
            delay(800)
            if (codigo == "123456") {
                _estado.value = AuthEstado.Exitoso
            } else {
                _estado.value = AuthEstado.Error("Código incorrecto")
            }
        }
    }

    // Validaciones en tiempo real
    fun validarEmail(email: String) {
        _emailValido.value =
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validarContrasena(contrasena: String) {
        _contrasenaValida.value = contrasena.length >= 6
    }

    fun resetearEstado() {
        _estado.value = AuthEstado.Inactivo
    }
    // CAMBIAR CONTRASEÑA
    fun cambiarContrasena(nuevaContrasena: String) {
        viewModelScope.launch {
            _estado.value = AuthEstado.Cargando
            delay(1500)
            _estado.value = AuthEstado.Exitoso
        }
    }
}
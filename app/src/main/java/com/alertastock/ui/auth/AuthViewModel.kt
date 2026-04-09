package com.alertastock.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthEstado {
    object Inactivo : AuthEstado()
    object Cargando : AuthEstado()
    object Exitoso : AuthEstado()
    data class Error(val mensaje: String) : AuthEstado()
}

class AuthViewModel : ViewModel() {

    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    private val _estado = MutableLiveData<AuthEstado>(AuthEstado.Inactivo)
    val estado: LiveData<AuthEstado> = _estado

    private val _emailValido = MutableLiveData(false)
    val emailValido: LiveData<Boolean> = _emailValido

    private val _contrasenaValida = MutableLiveData(false)
    val contrasenaValida: LiveData<Boolean> = _contrasenaValida

    // LOGIN REAL con Firebase
    fun iniciarSesion(email: String, contrasena: String) {
        if (email.isBlank() || contrasena.isBlank()) {
            _estado.value = AuthEstado.Error("Completa todos los campos")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.value = AuthEstado.Error("Correo electronico invalido")
            return
        }
        if (contrasena.length < 6) {
            _estado.value = AuthEstado.Error("La contrasena debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.signInWithEmailAndPassword(email, contrasena).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("no user record") == true ->
                            "No existe una cuenta con este correo"
                        e.message?.contains("password is invalid") == true ->
                            "Contrasena incorrecta"
                        e.message?.contains("network") == true ->
                            "Sin conexion a internet"
                        else -> "Error al iniciar sesion"
                    }
                )
            }
        }
    }

    // REGISTRO REAL con Firebase
    fun registrar(nombre: String, email: String, contrasena: String) {
        if (nombre.isBlank() || email.isBlank() || contrasena.isBlank()) {
            _estado.value = AuthEstado.Error("Completa todos los campos")
            return
        }
        if (contrasena.length < 8) {
            _estado.value = AuthEstado.Error("La contrasena debe tener al menos 8 caracteres")
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                // Crear usuario en Firebase
                val resultado = auth.createUserWithEmailAndPassword(email, contrasena).await()
                // Actualizar nombre del usuario
                val perfil = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()
                resultado.user?.updateProfile(perfil)?.await()
                // Enviar correo de verificación
                resultado.user?.sendEmailVerification()?.await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("email address is already") == true ->
                            "Este correo ya tiene una cuenta registrada"
                        e.message?.contains("network") == true ->
                            "Sin conexion a internet"
                        else -> "Error al crear la cuenta"
                    }
                )
            }
        }
    }

    // RECUPERAR CONTRASEÑA REAL con Firebase
    fun recuperarContrasena(email: String) {
        if (email.isBlank()) {
            _estado.value = AuthEstado.Error("Ingresa tu correo electronico")
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.sendPasswordResetEmail(email).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("no user record") == true ->
                            "No existe una cuenta con este correo"
                        e.message?.contains("network") == true ->
                            "Sin conexion a internet"
                        else -> "Error al enviar el correo"
                    }
                )
            }
        }
    }

    // LOGIN CON GOOGLE
    fun loginConGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                val credencial = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credencial).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error("Error al iniciar sesion con Google")
            }
        }
    }

    // CAMBIAR CONTRASEÑA
    fun cambiarContrasena(nuevaContrasena: String) {
        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.currentUser?.updatePassword(nuevaContrasena)?.await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error("Error al cambiar la contrasena")
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

    // Verificar si ya hay sesión activa
    fun hayUsuarioActivo(): Boolean {
        return auth.currentUser != null
    }
}
package com.alertastock.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.R
import com.alertastock.ui.auth.AuthError
import com.alertastock.ui.auth.AuthEstado
import com.alertastock.ui.auth.AuthViewModel
import com.alertastock.ui.theme.*
import com.alertastock.utils.PasswordConstants

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onAtras: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var negocio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }

    val estado by viewModel.estado.observeAsState()

    LaunchedEffect(estado) {
        if (estado is AuthEstado.Exitoso) {
            onRegistroExitoso()
            viewModel.resetearEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.Padding.xxl)
    ) {
        Spacer(modifier = Modifier.height(Dimensions.Size.spacerLarge))

        IconButton(onClick = onAtras) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_atras), tint = TextPrimary)
        }

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerLarge))

        Text(text = stringResource(R.string.registro_titulo), fontSize = Dimensions.Typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        Text(text = stringResource(R.string.registro_subtitulo), fontSize = Dimensions.Typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = Dimensions.Padding.xs))

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerMassive))

        // Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.label_nombre)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadius.lg),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerMedium))

        // Campo Negocio
        OutlinedTextField(
            value = negocio,
            onValueChange = { negocio = it },
            label = { Text(stringResource(R.string.label_negocio)) },
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadius.lg),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerMedium))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_correo_electronico)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextHint) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadius.lg),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerMedium))

        // Campo Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text(stringResource(R.string.label_contrasena_registro)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextHint) },
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Icon(
                        if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, tint = TextHint
                    )
                }
            },
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadius.lg),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerLarge))

        // Indicador fortaleza
        val fortaleza = PasswordConstants.evaluateStrength(contrasena)
        val colorFortaleza = when (fortaleza) {
            PasswordConstants.STRENGTH_STRONG -> Green
            PasswordConstants.STRENGTH_MEDIUM -> Yellow
            PasswordConstants.STRENGTH_WEAK -> Red
            else -> BorderMedium
        }
        val textoFortaleza = when (fortaleza) {
            PasswordConstants.STRENGTH_STRONG -> stringResource(R.string.fortaleza_fuerte)
            PasswordConstants.STRENGTH_MEDIUM -> stringResource(R.string.fortaleza_media)
            PasswordConstants.STRENGTH_WEAK -> stringResource(R.string.fortaleza_debil)
            else -> stringResource(R.string.fortaleza_vacia)
        }

        LinearProgressIndicator(
            progress = { fortaleza / 3f },
            modifier = Modifier.fillMaxWidth(),
            color = colorFortaleza,
            trackColor = BorderMedium
        )

        Text(text = textoFortaleza, color = colorFortaleza, fontSize = Dimensions.Typography.bodyTiny, modifier = Modifier.padding(top = Dimensions.Padding.xs))

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerLarge))

        Text(text = stringResource(R.string.txt_terminos), color = TextSecondary, fontSize = Dimensions.Typography.bodyTiny)

        Spacer(modifier = Modifier.height(Dimensions.Size.spacerXLarge))

        // Botón Registrarse
        Button(
            onClick = { viewModel.registrar(nombre, email, contrasena) },
            modifier = Modifier.fillMaxWidth().height(Dimensions.Size.buttonDefault),
            shape = RoundedCornerShape(Dimensions.CornerRadius.lg),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            enabled = estado !is AuthEstado.Cargando
        ) {
            if (estado is AuthEstado.Cargando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(Dimensions.Size.iconMedium))
            } else {
                Text(text = stringResource(R.string.btn_crear_cuenta), fontSize = Dimensions.Typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Error
        if (estado is AuthEstado.Error) {
            Spacer(modifier = Modifier.height(Dimensions.Size.spacerSmall))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(Dimensions.CornerRadius.md)
            ) {
                Text(
                    text = mensajeDeError((estado as AuthEstado.Error).mensaje),
                    color = Red,
                    fontSize = Dimensions.Typography.bodyExtraSmall,
                    modifier = Modifier.padding(Dimensions.Padding.md)
                )
            }
        }
    }
}
